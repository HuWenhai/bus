package org.aoju.bus.gitlab;

import org.aoju.bus.gitlab.models.OauthTokenResponse;
import org.aoju.bus.gitlab.models.Session;
import org.aoju.bus.gitlab.models.User;
import org.aoju.bus.gitlab.models.Version;
import org.aoju.bus.gitlab.utils.MaskingLoggingFilter;
import org.aoju.bus.gitlab.utils.Oauth2LoginStreamingOutput;
import org.aoju.bus.gitlab.utils.SecretString;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is provides a simplified interface to a GitLab API server, and divides the API up into
 * a separate API class for each concern.
 */
public class GitLabApi {

    /**
     * GitLab4J default per page.  GitLab will ignore anything over 100.
     */
    public static final int DEFAULT_PER_PAGE = 96;
    private final static Logger LOGGER = Logger.getLogger(GitLabApi.class.getName());
    // Used to keep track of GitLabApiExceptions on calls that return Optional<?>
    private static final Map<Integer, GitLabApiException> optionalExceptionMap =
            Collections.synchronizedMap(new WeakHashMap<Integer, GitLabApiException>());
    GitLabApiClient apiClient;
    private ApiVersion apiVersion;
    private String gitLabServerUrl;
    private Map<String, Object> clientConfigProperties;
    private int defaultPerPage = DEFAULT_PER_PAGE;
    private Session session;
    private ApplicationsApi applicationsApi;
    private ApplicationSettingsApi applicationSettingsApi;
    private AwardEmojiApi awardEmojiApi;
    private BoardsApi boardsApi;
    private CommitsApi commitsApi;
    private ContainerRegistryApi containerRegistryApi;
    private DiscussionsApi discussionsApi;
    private DeployKeysApi deployKeysApi;
    private EnvironmentsApi environmentsApi;
    private EpicsApi epicsApi;
    private EventsApi eventsApi;
    private GroupApi groupApi;
    private HealthCheckApi healthCheckApi;
    private ImportExportApi importExportApi;
    private IssuesApi issuesApi;
    private JobApi jobApi;
    private LabelsApi labelsApi;
    private LicenseApi licenseApi;
    private LicenseTemplatesApi licenseTemplatesApi;
    private MarkdownApi markdownApi;
    private MergeRequestApi mergeRequestApi;
    private MilestonesApi milestonesApi;
    private NamespaceApi namespaceApi;
    private NotesApi notesApi;
    private NotificationSettingsApi notificationSettingsApi;
    private PackagesApi packagesApi;
    private PipelineApi pipelineApi;
    private ProjectApi projectApi;
    private ProtectedBranchesApi protectedBranchesApi;
    private RepositoryApi repositoryApi;
    private RepositoryFileApi repositoryFileApi;
    private RunnersApi runnersApi;
    private SearchApi searchApi;
    private ServicesApi servicesApi;
    private SessionApi sessionApi;
    private SnippetsApi snippetsApi;
    private SystemHooksApi systemHooksApi;
    private TagsApi tagsApi;
    private TodosApi todosApi;
    private UserApi userApi;
    private WikisApi wikisApi;

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using the specified GitLab API version.
     *
     * @param apiVersion the ApiVersion specifying which version of the API to use
     * @param hostUrl    the URL of the GitLab server
     * @param tokenType  the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken  the token to use for access to the API
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, Constants.TokenType tokenType, String authToken) {
        this(apiVersion, hostUrl, tokenType, authToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using the specified GitLab API version.
     *
     * @param apiVersion   the ApiVersion specifying which version of the API to use
     * @param hostUrl      the URL of the GitLab server
     * @param privateToken to private token to use for access to the API
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, String privateToken) {
        this(apiVersion, hostUrl, privateToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl   the URL of the GitLab server
     * @param tokenType the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken the token to use for access to the API
     */
    public GitLabApi(String hostUrl, Constants.TokenType tokenType, String authToken) {
        this(ApiVersion.V4, hostUrl, tokenType, authToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl      the URL of the GitLab server
     * @param privateToken to private token to use for access to the API
     */
    public GitLabApi(String hostUrl, String privateToken) {
        this(ApiVersion.V4, hostUrl, privateToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using the specified GitLab API version.
     *
     * @param apiVersion the ApiVersion specifying which version of the API to use
     * @param hostUrl    the URL of the GitLab server
     * @param session    the Session instance obtained by logining into the GitLab server
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, Session session) {
        this(apiVersion, hostUrl, Constants.TokenType.PRIVATE, session.getPrivateToken(), null);
        this.session = session;
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl the URL of the GitLab server
     * @param session the Session instance obtained by logining into the GitLab server
     */
    public GitLabApi(String hostUrl, Session session) {
        this(ApiVersion.V4, hostUrl, session);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using the specified GitLab API version.
     *
     * @param apiVersion  the ApiVersion specifying which version of the API to use
     * @param hostUrl     the URL of the GitLab server
     * @param tokenType   the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken   the token to use for access to the API
     * @param secretToken use this token to validate received payloads
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, Constants.TokenType tokenType, String authToken, String secretToken) {
        this(apiVersion, hostUrl, tokenType, authToken, secretToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using the specified GitLab API version.
     *
     * @param apiVersion   the ApiVersion specifying which version of the API to use
     * @param hostUrl      the URL of the GitLab server
     * @param privateToken to private token to use for access to the API
     * @param secretToken  use this token to validate received payloads
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, String privateToken, String secretToken) {
        this(apiVersion, hostUrl, privateToken, secretToken, null);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl     the URL of the GitLab server
     * @param tokenType   the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken   the token to use for access to the API
     * @param secretToken use this token to validate received payloads
     */
    public GitLabApi(String hostUrl, Constants.TokenType tokenType, String authToken, String secretToken) {
        this(ApiVersion.V4, hostUrl, tokenType, authToken, secretToken);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl      the URL of the GitLab server
     * @param privateToken to private token to use for access to the API
     * @param secretToken  use this token to validate received payloads
     */
    public GitLabApi(String hostUrl, String privateToken, String secretToken) {
        this(ApiVersion.V4, hostUrl, Constants.TokenType.PRIVATE, privateToken, secretToken);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server specified by GitLab API version.
     *
     * @param apiVersion             the ApiVersion specifying which version of the API to use
     * @param hostUrl                the URL of the GitLab server
     * @param privateToken           to private token to use for access to the API
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties Map instance with additional properties for the Jersey client connection
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, String privateToken, String secretToken, Map<String, Object> clientConfigProperties) {
        this(apiVersion, hostUrl, Constants.TokenType.PRIVATE, privateToken, secretToken, clientConfigProperties);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl                the URL of the GitLab server
     * @param tokenType              the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken              the token to use for access to the API
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties Map instance with additional properties for the Jersey client connection
     */
    public GitLabApi(String hostUrl, Constants.TokenType tokenType, String authToken, String secretToken, Map<String, Object> clientConfigProperties) {
        this(ApiVersion.V4, hostUrl, tokenType, authToken, secretToken, clientConfigProperties);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl                the URL of the GitLab server
     * @param privateToken           to private token to use for access to the API
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties Map instance with additional properties for the Jersey client connection
     */
    public GitLabApi(String hostUrl, String privateToken, String secretToken, Map<String, Object> clientConfigProperties) {
        this(ApiVersion.V4, hostUrl, Constants.TokenType.PRIVATE, privateToken, secretToken, clientConfigProperties);
    }

    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server specified by GitLab API version.
     *
     * @param apiVersion             the ApiVersion specifying which version of the API to use
     * @param hostUrl                the URL of the GitLab server
     * @param tokenType              the type of auth the token is for, PRIVATE or ACCESS
     * @param authToken              to token to use for access to the API
     * @param secretToken            use this token to validate received payloads
     * @param clientConfigProperties Map instance with additional properties for the Jersey client connection
     */
    public GitLabApi(ApiVersion apiVersion, String hostUrl, Constants.TokenType tokenType, String authToken, String secretToken, Map<String, Object> clientConfigProperties) {
        this.apiVersion = apiVersion;
        this.gitLabServerUrl = hostUrl;
        this.clientConfigProperties = clientConfigProperties;
        apiClient = new GitLabApiClient(apiVersion, hostUrl, tokenType, authToken, secretToken, clientConfigProperties);
    }

    /**
     * Get the GitLab4J shared Logger instance.
     *
     * @return the GitLab4J shared Logger instance
     */
    public static final Logger getLogger() {
        return (LOGGER);
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url      GitLab URL
     * @param username user name for which private token should be obtained
     * @param password a CharSequence containing the password for a given {@code username}
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, CharSequence password) throws GitLabApiException {
        return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, password, null, null, false));
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url      GitLab URL
     * @param username user name for which private token should be obtained
     * @param password a char array holding the password for a given {@code username}
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, char[] password) throws GitLabApiException {

        try (SecretString secretPassword = new SecretString(password)) {
            return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, secretPassword, null, null, false));
        }
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                a CharSequence containing the password for a given {@code username}
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, CharSequence password, boolean ignoreCertificateErrors) throws GitLabApiException {
        return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, password, null, null, ignoreCertificateErrors));
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                a char array holding the password for a given {@code username}
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, char[] password, boolean ignoreCertificateErrors) throws GitLabApiException {

        try (SecretString secretPassword = new SecretString(password)) {
            return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, secretPassword, null, null, ignoreCertificateErrors));
        }
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                a CharSequence containing the password for a given {@code username}
     * @param secretToken             use this token to validate received payloads
     * @param clientConfigProperties  Map instance with additional properties for the Jersey client connection
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, CharSequence password, String secretToken,
                                        Map<String, Object> clientConfigProperties, boolean ignoreCertificateErrors) throws GitLabApiException {
        return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, password, secretToken, clientConfigProperties, ignoreCertificateErrors));
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                a char array holding the password for a given {@code username}
     * @param secretToken             use this token to validate received payloads
     * @param clientConfigProperties  Map instance with additional properties for the Jersey client connection
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(String url, String username, char[] password, String secretToken,
                                        Map<String, Object> clientConfigProperties, boolean ignoreCertificateErrors) throws GitLabApiException {

        try (SecretString secretPassword = new SecretString(password)) {
            return (GitLabApi.oauth2Login(ApiVersion.V4, url, username, secretPassword,
                    secretToken, clientConfigProperties, ignoreCertificateErrors));
        }
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param apiVersion              the ApiVersion specifying which version of the API to use
     * @param username                user name for which private token should be obtained
     * @param password                a char array holding the password for a given {@code username}
     * @param secretToken             use this token to validate received payloads
     * @param clientConfigProperties  Map instance with additional properties for the Jersey client connection
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(ApiVersion apiVersion, String url, String username, char[] password, String secretToken,
                                        Map<String, Object> clientConfigProperties, boolean ignoreCertificateErrors) throws GitLabApiException {

        try (SecretString secretPassword = new SecretString(password)) {
            return (GitLabApi.oauth2Login(apiVersion, url, username, secretPassword,
                    secretToken, clientConfigProperties, ignoreCertificateErrors));
        }
    }

    /**
     * <p>Logs into GitLab using OAuth2 with the provided {@code username} and {@code password},
     * and creates a new {@code GitLabApi} instance using returned access token.</p>
     *
     * @param url                     GitLab URL
     * @param apiVersion              the ApiVersion specifying which version of the API to use
     * @param username                user name for which private token should be obtained
     * @param password                password for a given {@code username}
     * @param secretToken             use this token to validate received payloads
     * @param clientConfigProperties  Map instance with additional properties for the Jersey client connection
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     */
    public static GitLabApi oauth2Login(ApiVersion apiVersion, String url, String username, CharSequence password,
                                        String secretToken, Map<String, Object> clientConfigProperties, boolean ignoreCertificateErrors) throws GitLabApiException {

        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("both username and email cannot be empty or null");
        }

        // Create a GitLabApi instance set up to be used to do an OAUTH2 login.
        GitLabApi gitLabApi = new GitLabApi(apiVersion, url, (String) null);
        gitLabApi.apiClient.setHostUrlToBaseUrl();

        if (ignoreCertificateErrors) {
            gitLabApi.setIgnoreCertificateErrors(true);
        }

        class Oauth2Api extends org.aoju.bus.gitlab.AbstractApi {
            Oauth2Api(GitLabApi gitlabApi) {
                super(gitlabApi);
            }
        }

        try (Oauth2LoginStreamingOutput stream = new Oauth2LoginStreamingOutput(username, password)) {

            Response response = new Oauth2Api(gitLabApi).post(Response.Status.OK, stream, MediaType.APPLICATION_JSON, "oauth", "token");
            OauthTokenResponse oauthToken = response.readEntity(OauthTokenResponse.class);
            gitLabApi = new GitLabApi(apiVersion, url, Constants.TokenType.OAUTH2_ACCESS, oauthToken.getAccessToken(), secretToken, clientConfigProperties);
            if (ignoreCertificateErrors) {
                gitLabApi.setIgnoreCertificateErrors(true);
            }

            return (gitLabApi);
        }
    }

    /**
     * <p>Logs into GitLab using provided {@code username} and {@code password}, and creates a new {@code GitLabApi} instance
     * using returned private token and the specified GitLab API version.</p>
     *
     * <strong>NOTE</strong>: For GitLab servers 10.2 and above this will utilize OAUTH2 for login.  For GitLab servers prior to
     * 10.2, the Session API login is utilized.
     *
     * @param apiVersion the ApiVersion specifying which version of the API to use
     * @param url        GitLab URL
     * @param username   user name for which private token should be obtained
     * @param password   password for a given {@code username}
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     * @deprecated As of release 4.8.7, will be removed in 4.9.0
     */
    @Deprecated
    public static GitLabApi login(ApiVersion apiVersion, String url, String username, String password) throws GitLabApiException {
        return (GitLabApi.login(apiVersion, url, username, password, false));
    }

    /**
     * <p>Logs into GitLab using provided {@code username} and {@code password}, and creates a new {@code GitLabApi} instance
     * using returned private token using GitLab API version 4.</p>
     *
     * <strong>NOTE</strong>: For GitLab servers 10.2 and above this will utilize OAUTH2 for login.  For GitLab servers prior to
     * 10.2, the Session API login is utilized.
     *
     * @param url      GitLab URL
     * @param username user name for which private token should be obtained
     * @param password password for a given {@code username}
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     * @deprecated As of release 4.8.7, will be removed in 4.9.0
     */
    @Deprecated
    public static GitLabApi login(String url, String username, String password) throws GitLabApiException {
        return (GitLabApi.login(ApiVersion.V4, url, username, password, false));
    }

    /**
     * <p>Logs into GitLab using provided {@code username} and {@code password}, and creates a new {@code GitLabApi} instance
     * using returned private token and the specified GitLab API version.</p>
     *
     * <strong>NOTE</strong>: For GitLab servers 10.2 and above this will utilize OAUTH2 for login.  For GitLab servers prior to
     * 10.2, the Session API login is utilized.
     *
     * @param apiVersion              the ApiVersion specifying which version of the API to use
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                password for a given {@code username}
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     * @deprecated As of release 4.8.7, will be removed in 4.9.0
     */
    @Deprecated
    public static GitLabApi login(ApiVersion apiVersion, String url, String username, String password, boolean ignoreCertificateErrors) throws GitLabApiException {

        GitLabApi gitLabApi = new GitLabApi(apiVersion, url, (String) null);
        if (ignoreCertificateErrors) {
            gitLabApi.setIgnoreCertificateErrors(true);
        }

        try {

            SessionApi sessionApi = gitLabApi.getSessionApi();
            Session session = sessionApi.login(username, null, password);
            gitLabApi = new GitLabApi(apiVersion, url, session);

            if (ignoreCertificateErrors) {
                gitLabApi.setIgnoreCertificateErrors(true);
            }

        } catch (GitLabApiException gle) {
            if (gle.getHttpStatus() != Response.Status.NOT_FOUND.getStatusCode()) {
                throw (gle);
            } else {
                gitLabApi = GitLabApi.oauth2Login(apiVersion, url, username, password, null, null, ignoreCertificateErrors);
            }
        }

        return (gitLabApi);
    }

    /**
     * <p>Logs into GitLab using provided {@code username} and {@code password}, and creates a new {@code GitLabApi} instance
     * using returned private token using GitLab API version 4.</p>
     *
     * <strong>NOTE</strong>: For GitLab servers 10.2 and above this will utilize OAUTH2 for login.  For GitLab servers prior to
     * 10.2, the Session API login is utilized.
     *
     * @param url                     GitLab URL
     * @param username                user name for which private token should be obtained
     * @param password                password for a given {@code username}
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     * @return new {@code GitLabApi} instance configured for a user-specific token
     * @throws GitLabApiException GitLabApiException if any exception occurs during execution
     * @deprecated As of release 4.8.7, will be removed in 4.9.0
     */
    @Deprecated
    public static GitLabApi login(String url, String username, String password, boolean ignoreCertificateErrors) throws GitLabApiException {
        return (GitLabApi.login(ApiVersion.V4, url, username, password, ignoreCertificateErrors));
    }

    /**
     * Create and return an Optional instance associated with a GitLabApiException.
     *
     * @param <T>  the type of the Optional instance
     * @param glae the GitLabApiException that was the result of a call to the GitLab API
     * @return the created Optional instance
     */
    protected static final <T> Optional<T> createOptionalFromException(GitLabApiException glae) {
        Optional<T> optional = Optional.empty();
        optionalExceptionMap.put(System.identityHashCode(optional), glae);
        return (optional);
    }

    /**
     * Get the exception associated with the provided Optional instance, or null if no exception is
     * associated with the Optional instance.
     *
     * @param optional the Optional instance to get the exception for
     * @return the exception associated with the provided Optional instance, or null if no exception is
     * associated with the Optional instance
     */
    public static final GitLabApiException getOptionalException(Optional<?> optional) {
        return (optionalExceptionMap.get(System.identityHashCode(optional)));
    }

    /**
     * Return the Optional instances contained value, if present, otherwise throw the exception that is
     * associated with the Optional instance.
     *
     * @param <T>      the type for the Optional parameter
     * @param optional the Optional instance to get the value for
     * @return the value of the Optional instance if no exception is associated with it
     * @throws GitLabApiException if there was an exception associated with the Optional instance
     */
    public static final <T> T orElseThrow(Optional<T> optional) throws GitLabApiException {

        GitLabApiException glea = getOptionalException(optional);
        if (glea != null) {
            throw (glea);
        }

        return (optional.get());
    }

    /**
     * Create a new GitLabApi instance that is logically a duplicate of this instance, with the exception off sudo state.
     *
     * @return a new GitLabApi instance that is logically a duplicate of this instance, with the exception off sudo state.
     */
    public final GitLabApi duplicate() {

        Integer sudoUserId = this.getSudoAsId();
        GitLabApi gitLabApi = new GitLabApi(apiVersion, gitLabServerUrl,
                getTokenType(), getAuthToken(), getSecretToken(), clientConfigProperties);
        if (sudoUserId != null) {
            gitLabApi.apiClient.setSudoAsId(sudoUserId);
        }

        if (getIgnoreCertificateErrors()) {
            gitLabApi.setIgnoreCertificateErrors(true);
        }

        gitLabApi.defaultPerPage = this.defaultPerPage;
        return (gitLabApi);
    }

    /**
     * <p>If this instance was created with {@link #login(String, String, String)} this method will
     * return the Session instance returned by the GitLab API on login, otherwise returns null.</p>
     *
     * <strong>NOTE</strong>: For GitLab servers 10.2 and above this method will always return null.
     *
     * @return the Session instance
     * @deprecated This method will be removed in Release 4.9.0
     */
    @Deprecated
    public Session getSession() {
        return session;
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the GitLab4J shared Logger instance and Level.FINE as the level.
     *
     * @return this GitLabApi instance
     */
    public GitLabApi withRequestResponseLogging() {
        enableRequestResponseLogging();
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the GitLab4J shared Logger instance.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @return this GitLabApi instance
     */
    public GitLabApi withRequestResponseLogging(Level level) {
        enableRequestResponseLogging(level);
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API.
     *
     * @param logger the Logger instance to log to
     * @param level  the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @return this GitLabApi instance
     */
    public GitLabApi withRequestResponseLogging(Logger logger, Level level) {
        enableRequestResponseLogging(logger, level);
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the GitLab4J shared Logger instance and Level.FINE as the level.
     */
    public void enableRequestResponseLogging() {
        enableRequestResponseLogging(LOGGER, Level.FINE);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the GitLab4J shared Logger instance. Logging will NOT include entity logging and
     * will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     */
    public void enableRequestResponseLogging(Level level) {
        enableRequestResponseLogging(LOGGER, level, 0);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger. Logging will NOT include entity logging and will mask PRIVATE-TOKEN
     * and Authorization headers..
     *
     * @param logger the Logger instance to log to
     * @param level  the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     */
    public void enableRequestResponseLogging(Logger logger, Level level) {
        enableRequestResponseLogging(logger, level, 0);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * GitLab4J shared Logger instance. Logging will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param level         the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     *                      is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     *                      the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     */
    public void enableRequestResponseLogging(Level level, int maxEntitySize) {
        enableRequestResponseLogging(LOGGER, level, maxEntitySize);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger. Logging will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param logger        the Logger instance to log to
     * @param level         the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     *                      is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     *                      the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     */
    public void enableRequestResponseLogging(Logger logger, Level level, int maxEntitySize) {
        enableRequestResponseLogging(logger, level, maxEntitySize, MaskingLoggingFilter.DEFAULT_MASKED_HEADER_NAMES);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * GitLab4J shared Logger instance.
     *
     * @param level             the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Level level, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(LOGGER, level, 0, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger.
     *
     * @param logger            the Logger instance to log to
     * @param level             the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Logger logger, Level level, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(logger, level, 0, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * GitLab4J shared Logger instance.
     *
     * @param level             the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize     maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     *                          is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     *                          the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Level level, int maxEntitySize, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(LOGGER, level, maxEntitySize, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger.
     *
     * @param logger            the Logger instance to log to
     * @param level             the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize     maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     *                          is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     *                          the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Logger logger, Level level, int maxEntitySize, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(logger, level, maxEntitySize, maskedHeaderNames);
    }

    /**
     * Sets up all future calls to the GitLab API to be done as another user specified by sudoAsUsername.
     * To revert back to normal non-sudo operation you must call unsudo(), or pass null as the username.
     *
     * @param sudoAsUsername the username to sudo as, null will turn off sudo
     * @throws GitLabApiException if any exception occurs
     */
    public void sudo(String sudoAsUsername) throws GitLabApiException {

        if (sudoAsUsername == null || sudoAsUsername.trim().length() == 0) {
            apiClient.setSudoAsId(null);
            return;
        }

        // Get the User specified by username, if you are not an admin or the username is not found, this will fail
        User user = getUserApi().getUser(sudoAsUsername);
        if (user == null || user.getId() == null) {
            throw new GitLabApiException("the specified username was not found");
        }

        Integer sudoAsId = user.getId();
        apiClient.setSudoAsId(sudoAsId);
    }

    /**
     * Turns off the currently configured sudo as ID.
     */
    public void unsudo() {
        apiClient.setSudoAsId(null);
    }

    /**
     * Get the current sudo as ID, will return null if not in sudo mode.
     *
     * @return the current sudo as ID, will return null if not in sudo mode
     */
    public Integer getSudoAsId() {
        return (apiClient.getSudoAsId());
    }

    /**
     * Sets up all future calls to the GitLab API to be done as another user specified by provided user ID.
     * To revert back to normal non-sudo operation you must call unsudo(), or pass null as the sudoAsId.
     *
     * @param sudoAsId the ID of the user to sudo as, null will turn off sudo
     * @throws GitLabApiException if any exception occurs
     */
    public void setSudoAsId(Integer sudoAsId) throws GitLabApiException {

        if (sudoAsId == null) {
            apiClient.setSudoAsId(null);
            return;
        }

        // Get the User specified by the sudoAsId, if you are not an admin or the username is not found, this will fail
        User user = getUserApi().getUser(sudoAsId);
        if (user == null || !user.getId().equals(sudoAsId)) {
            throw new GitLabApiException("the specified user ID was not found");
        }

        apiClient.setSudoAsId(sudoAsId);
    }

    /**
     * Get the auth token being used by this client.
     *
     * @return the auth token being used by this client
     */
    public String getAuthToken() {
        return (apiClient.getAuthToken());
    }

    /**
     * Get the secret token.
     *
     * @return the secret token
     */
    public String getSecretToken() {
        return (apiClient.getSecretToken());
    }

    /**
     * Get the TokenType this client is using.
     *
     * @return the TokenType this client is using
     */
    public Constants.TokenType getTokenType() {
        return (apiClient.getTokenType());
    }

    /**
     * Return the GitLab API version that this instance is using.
     *
     * @return the GitLab API version that this instance is using
     */
    public ApiVersion getApiVersion() {
        return (apiVersion);
    }

    /**
     * Get the URL to the GitLab server.
     *
     * @return the URL to the GitLab server
     */
    public String getGitLabServerUrl() {
        return (gitLabServerUrl);
    }

    /**
     * Get the default number per page for calls that return multiple items.
     *
     * @return the default number per page for calls that return multiple item
     */
    public int getDefaultPerPage() {
        return (defaultPerPage);
    }

    /**
     * Set the default number per page for calls that return multiple items.
     *
     * @param defaultPerPage the new default number per page for calls that return multiple item
     */
    public void setDefaultPerPage(int defaultPerPage) {
        this.defaultPerPage = defaultPerPage;
    }

    /**
     * Return the GitLabApiClient associated with this instance. This is used by all the sub API classes
     * to communicate with the GitLab API.
     *
     * @return the GitLabApiClient associated with this instance
     */
    GitLabApiClient getApiClient() {
        return (apiClient);
    }

    /**
     * Returns true if the API is setup to ignore SSL certificate errors, otherwise returns false.
     *
     * @return true if the API is setup to ignore SSL certificate errors, otherwise returns false
     */
    public boolean getIgnoreCertificateErrors() {
        return (apiClient.getIgnoreCertificateErrors());
    }

    /**
     * Sets up the Jersey system ignore SSL certificate errors or not.
     *
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     */
    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
        apiClient.setIgnoreCertificateErrors(ignoreCertificateErrors);
    }

    /**
     * Get the version info for the GitLab server using the GitLab Version API.
     *
     * @return the version info for the GitLab server
     * @throws GitLabApiException if any exception occurs
     */
    public Version getVersion() throws GitLabApiException {

        class VersionApi extends AbstractApi {
            VersionApi(GitLabApi gitlabApi) {
                super(gitlabApi);
            }
        }

        Response response = new VersionApi(this).get(Response.Status.OK, null, "version");
        return (response.readEntity(Version.class));
    }

    /**
     * Gets the ApplicationsApi instance owned by this GitLabApi instance. The ApplicationsApi is used
     * to perform all OAUTH application related API calls.
     *
     * @return the ApplicationsApi instance owned by this GitLabApi instance
     */
    public ApplicationsApi getApplicationsApi() {

        if (applicationsApi == null) {
            synchronized (this) {
                if (applicationsApi == null) {
                    applicationsApi = new ApplicationsApi(this);
                }
            }
        }

        return (applicationsApi);
    }

    /**
     * Gets the ApplicationSettingsApi instance owned by this GitLabApi instance. The ApplicationSettingsApi is used
     * to perform all application settingsrelated API calls.
     *
     * @return the ApplicationsApi instance owned by this GitLabApi instance
     */
    public ApplicationSettingsApi getApplicationSettingsApi() {

        if (applicationSettingsApi == null) {
            synchronized (this) {
                if (applicationSettingsApi == null) {
                    applicationSettingsApi = new ApplicationSettingsApi(this);
                }
            }
        }

        return (applicationSettingsApi);
    }

    /**
     * Gets the AwardEmojiApi instance owned by this GitLabApi instance. The AwardEmojiApi is used
     * to perform all award emoji related API calls.
     *
     * @return the AwardEmojiApi instance owned by this GitLabApi instance
     */
    public AwardEmojiApi getAwardEmojiApi() {

        if (awardEmojiApi == null) {
            synchronized (this) {
                if (awardEmojiApi == null) {
                    awardEmojiApi = new AwardEmojiApi(this);
                }
            }
        }

        return (awardEmojiApi);
    }

    /**
     * Gets the BoardsApi instance owned by this GitLabApi instance. The BoardsApi is used
     * to perform all Issue Boards related API calls.
     *
     * @return the BoardsApi instance owned by this GitLabApi instance
     */
    public BoardsApi getBoardsApi() {

        if (boardsApi == null) {
            synchronized (this) {
                if (boardsApi == null) {
                    boardsApi = new BoardsApi(this);
                }
            }
        }

        return (boardsApi);
    }

    /**
     * Gets the CommitsApi instance owned by this GitLabApi instance. The CommitsApi is used
     * to perform all commit related API calls.
     *
     * @return the CommitsApi instance owned by this GitLabApi instance
     */
    public CommitsApi getCommitsApi() {

        if (commitsApi == null) {
            synchronized (this) {
                if (commitsApi == null) {
                    commitsApi = new CommitsApi(this);
                }
            }
        }

        return (commitsApi);
    }

    /**
     * Gets the ContainerRegistryApi instance owned by this GitLabApi instance. The ContainerRegistryApi is used
     * to perform all Docker Registry related API calls.
     *
     * @return the ContainerRegistryApi instance owned by this GitLabApi instance
     */
    public ContainerRegistryApi getContainerRegistryApi() {

        if (containerRegistryApi == null) {
            synchronized (this) {
                if (containerRegistryApi == null) {
                    containerRegistryApi = new ContainerRegistryApi(this);
                }
            }
        }

        return (containerRegistryApi);
    }

    /**
     * Gets the DeployKeysApi instance owned by this GitLabApi instance. The DeployKeysApi is used
     * to perform all deploy key related API calls.
     *
     * @return the DeployKeysApi instance owned by this GitLabApi instance
     */
    public DeployKeysApi getDeployKeysApi() {

        if (deployKeysApi == null) {
            synchronized (this) {
                if (deployKeysApi == null) {
                    deployKeysApi = new DeployKeysApi(this);
                }
            }
        }

        return (deployKeysApi);
    }

    /**
     * Gets the DiscussionsApi instance owned by this GitLabApi instance. The DiscussionsApi is used
     * to perform all discussion related API calls.
     *
     * @return the DiscussionsApi instance owned by this GitLabApi instance
     */
    public DiscussionsApi getDiscussionsApi() {

        if (discussionsApi == null) {
            synchronized (this) {
                if (discussionsApi == null) {
                    discussionsApi = new DiscussionsApi(this);
                }
            }
        }

        return (discussionsApi);
    }

    /**
     * Gets the EnvironmentsApi instance owned by this GitLabApi instance. The EnvironmentsApi is used
     * to perform all environment related API calls.
     *
     * @return the EnvironmentsApi instance owned by this GitLabApi instance
     */
    public EnvironmentsApi getEnvironmentsApi() {

        if (environmentsApi == null) {
            synchronized (this) {
                if (environmentsApi == null) {
                    environmentsApi = new EnvironmentsApi(this);
                }
            }
        }

        return (environmentsApi);
    }

    /**
     * Gets the EpicsApi instance owned by this GitLabApi instance. The EpicsApi is used
     * to perform all Epics and Epic Issues related API calls.
     *
     * @return the EpicsApi instance owned by this GitLabApi instance
     */
    public EpicsApi getEpicsApi() {

        if (epicsApi == null) {
            synchronized (this) {
                if (epicsApi == null) {
                    epicsApi = new EpicsApi(this);
                }
            }
        }

        return (epicsApi);
    }

    /**
     * Gets the EventsApi instance owned by this GitLabApi instance. The EventsApi is used
     * to perform all events related API calls.
     *
     * @return the EventsApi instance owned by this GitLabApi instance
     */
    public EventsApi getEventsApi() {

        if (eventsApi == null) {
            synchronized (this) {
                if (eventsApi == null) {
                    eventsApi = new EventsApi(this);
                }
            }
        }

        return (eventsApi);
    }

    /**
     * Gets the GroupApi instance owned by this GitLabApi instance. The GroupApi is used
     * to perform all group related API calls.
     *
     * @return the GroupApi instance owned by this GitLabApi instance
     */
    public GroupApi getGroupApi() {

        if (groupApi == null) {
            synchronized (this) {
                if (groupApi == null) {
                    groupApi = new GroupApi(this);
                }
            }
        }

        return (groupApi);
    }

    /**
     * Gets the HealthCheckApi instance owned by this GitLabApi instance. The HealthCheckApi is used
     * to perform all admin level gitlab health monitoring.
     *
     * @return the HealthCheckApi instance owned by this GitLabApi instance
     */
    public HealthCheckApi getHealthCheckApi() {

        if (healthCheckApi == null) {
            synchronized (this) {
                if (healthCheckApi == null) {
                    healthCheckApi = new HealthCheckApi(this);
                }
            }
        }

        return (healthCheckApi);
    }

    /**
     * Gets the ImportExportApi instance owned by this GitLabApi instance. The ImportExportApi is used
     * to perform all project import/export related API calls.
     *
     * @return the ImportExportApi instance owned by this GitLabApi instance
     */
    public ImportExportApi getImportExportApi() {

        if (importExportApi == null) {
            synchronized (this) {
                if (importExportApi == null) {
                    importExportApi = new ImportExportApi(this);
                }
            }
        }

        return (importExportApi);
    }

    /**
     * Gets the IssuesApi instance owned by this GitLabApi instance. The IssuesApi is used
     * to perform all issue related API calls.
     *
     * @return the IssuesApi instance owned by this GitLabApi instance
     */
    public IssuesApi getIssuesApi() {

        if (issuesApi == null) {
            synchronized (this) {
                if (issuesApi == null) {
                    issuesApi = new IssuesApi(this);
                }
            }
        }

        return (issuesApi);
    }

    /**
     * Gets the JobApi instance owned by this GitLabApi instance. The JobApi is used
     * to perform all jobs related API calls.
     *
     * @return the JobsApi instance owned by this GitLabApi instance
     */
    public JobApi getJobApi() {

        if (jobApi == null) {
            synchronized (this) {
                if (jobApi == null) {
                    jobApi = new JobApi(this);
                }
            }
        }

        return (jobApi);
    }

    public LabelsApi getLabelsApi() {

        if (labelsApi == null) {
            synchronized (this) {
                if (labelsApi == null) {
                    labelsApi = new LabelsApi(this);
                }
            }
        }

        return (labelsApi);
    }

    /**
     * Gets the LicenseApi instance owned by this GitLabApi instance. The LicenseApi is used
     * to perform all license related API calls.
     *
     * @return the LicenseApi instance owned by this GitLabApi instance
     */
    public LicenseApi getLicenseApi() {

        if (licenseApi == null) {
            synchronized (this) {
                if (licenseApi == null) {
                    licenseApi = new LicenseApi(this);
                }
            }
        }

        return (licenseApi);
    }

    /**
     * Gets the LicenseTemplatesApi instance owned by this GitLabApi instance. The LicenseTemplatesApi is used
     * to perform all license template related API calls.
     *
     * @return the LicenseTemplatesApi instance owned by this GitLabApi instance
     */
    public LicenseTemplatesApi getLicenseTemplatesApi() {

        if (licenseTemplatesApi == null) {
            synchronized (this) {
                if (licenseTemplatesApi == null) {
                    licenseTemplatesApi = new LicenseTemplatesApi(this);
                }
            }
        }

        return (licenseTemplatesApi);
    }

    /**
     * Gets the MarkdownApi instance owned by this GitLabApi instance. The MarkdownApi is used
     * to perform all markdown related API calls.
     *
     * @return the MarkdownApi instance owned by this GitLabApi instance
     */
    public MarkdownApi getMarkdownApi() {

        if (markdownApi == null) {
            synchronized (this) {
                if (markdownApi == null) {
                    markdownApi = new MarkdownApi(this);
                }
            }
        }

        return (markdownApi);
    }

    /**
     * Gets the MergeRequestApi instance owned by this GitLabApi instance. The MergeRequestApi is used
     * to perform all merge request related API calls.
     *
     * @return the MergeRequestApi instance owned by this GitLabApi instance
     */
    public MergeRequestApi getMergeRequestApi() {

        if (mergeRequestApi == null) {
            synchronized (this) {
                if (mergeRequestApi == null) {
                    mergeRequestApi = new MergeRequestApi(this);
                }
            }
        }

        return (mergeRequestApi);
    }

    /**
     * Gets the MilsestonesApi instance owned by this GitLabApi instance.
     *
     * @return the MilsestonesApi instance owned by this GitLabApi instance
     */
    public MilestonesApi getMilestonesApi() {

        if (milestonesApi == null) {
            synchronized (this) {
                if (milestonesApi == null) {
                    milestonesApi = new MilestonesApi(this);
                }
            }
        }

        return (milestonesApi);
    }

    /**
     * Gets the NamespaceApi instance owned by this GitLabApi instance. The NamespaceApi is used
     * to perform all namespace related API calls.
     *
     * @return the NamespaceApi instance owned by this GitLabApi instance
     */
    public NamespaceApi getNamespaceApi() {

        if (namespaceApi == null) {
            synchronized (this) {
                if (namespaceApi == null) {
                    namespaceApi = new NamespaceApi(this);
                }
            }
        }

        return (namespaceApi);
    }

    /**
     * Gets the NotesApi instance owned by this GitLabApi instance. The NotesApi is used
     * to perform all notes related API calls.
     *
     * @return the NotesApi instance owned by this GitLabApi instance
     */
    public NotesApi getNotesApi() {

        if (notesApi == null) {
            synchronized (this) {
                if (notesApi == null) {
                    notesApi = new NotesApi(this);
                }
            }
        }

        return (notesApi);
    }

    /**
     * Gets the NotesApi instance owned by this GitLabApi instance. The NotesApi is used
     * to perform all notes related API calls.
     *
     * @return the NotesApi instance owned by this GitLabApi instance
     */
    public NotificationSettingsApi getNotificationSettingsApi() {

        if (notificationSettingsApi == null) {
            synchronized (this) {
                if (notificationSettingsApi == null) {
                    notificationSettingsApi = new NotificationSettingsApi(this);
                }
            }
        }

        return (notificationSettingsApi);
    }

    /**
     * Gets the PackagesApi instance owned by this GitLabApi instance. The PackagesApi is used
     * to perform all Package related API calls.
     *
     * @return the PackagesApi instance owned by this GitLabApi instance
     */
    public PackagesApi getPackagesApi() {

        if (packagesApi == null) {
            synchronized (this) {
                if (packagesApi == null) {
                    packagesApi = new PackagesApi(this);
                }
            }
        }

        return (packagesApi);
    }

    /**
     * Gets the PipelineApi instance owned by this GitLabApi instance. The PipelineApi is used
     * to perform all pipeline related API calls.
     *
     * @return the PipelineApi instance owned by this GitLabApi instance
     */
    public PipelineApi getPipelineApi() {

        if (pipelineApi == null) {
            synchronized (this) {
                if (pipelineApi == null) {
                    pipelineApi = new PipelineApi(this);
                }
            }
        }

        return (pipelineApi);
    }

    /**
     * Gets the ProjectApi instance owned by this GitLabApi instance. The ProjectApi is used
     * to perform all project related API calls.
     *
     * @return the ProjectApi instance owned by this GitLabApi instance
     */
    public ProjectApi getProjectApi() {

        if (projectApi == null) {
            synchronized (this) {
                if (projectApi == null) {
                    projectApi = new ProjectApi(this);
                }
            }
        }

        return (projectApi);
    }

    /**
     * Gets the ProtectedBranchesApi instance owned by this GitLabApi instance. The ProtectedBranchesApi is used
     * to perform all protection related actions on a branch of a project.
     *
     * @return the ProtectedBranchesApi instance owned by this GitLabApi instance
     */
    public ProtectedBranchesApi getProtectedBranchesApi() {

        if (this.protectedBranchesApi == null) {
            synchronized (this) {
                if (this.protectedBranchesApi == null) {
                    this.protectedBranchesApi = new ProtectedBranchesApi(this);
                }
            }
        }

        return (this.protectedBranchesApi);
    }

    /**
     * Gets the RepositoryApi instance owned by this GitLabApi instance. The RepositoryApi is used
     * to perform all repository related API calls.
     *
     * @return the RepositoryApi instance owned by this GitLabApi instance
     */
    public RepositoryApi getRepositoryApi() {

        if (repositoryApi == null) {
            synchronized (this) {
                if (repositoryApi == null) {
                    repositoryApi = new RepositoryApi(this);
                }
            }
        }

        return (repositoryApi);
    }

    /**
     * Gets the RepositoryFileApi instance owned by this GitLabApi instance. The RepositoryFileApi is used
     * to perform all repository files related API calls.
     *
     * @return the RepositoryFileApi instance owned by this GitLabApi instance
     */
    public RepositoryFileApi getRepositoryFileApi() {

        if (repositoryFileApi == null) {
            synchronized (this) {
                if (repositoryFileApi == null) {
                    repositoryFileApi = new RepositoryFileApi(this);
                }
            }
        }

        return (repositoryFileApi);
    }

    /**
     * Gets the RunnersApi instance owned by this GitLabApi instance. The RunnersApi is used
     * to perform all Runner related API calls.
     *
     * @return the RunnerApi instance owned by this GitLabApi instance
     */
    public RunnersApi getRunnersApi() {

        if (runnersApi == null) {
            synchronized (this) {
                if (runnersApi == null) {
                    runnersApi = new RunnersApi(this);
                }
            }
        }

        return (runnersApi);
    }

    /**
     * Gets the SearchApi instance owned by this GitLabApi instance. The SearchApi is used
     * to perform search related API calls.
     *
     * @return the SearchApi instance owned by this GitLabApi instance
     */
    public SearchApi getSearchApi() {

        if (searchApi == null) {
            synchronized (this) {
                if (searchApi == null) {
                    searchApi = new SearchApi(this);
                }
            }
        }

        return (searchApi);
    }

    /**
     * Gets the ServicesApi instance owned by this GitLabApi instance. The ServicesApi is used
     * to perform all services related API calls.
     *
     * @return the ServicesApi instance owned by this GitLabApi instance
     */
    public ServicesApi getServicesApi() {

        if (servicesApi == null) {
            synchronized (this) {
                if (servicesApi == null) {
                    servicesApi = new ServicesApi(this);
                }
            }
        }

        return (servicesApi);
    }

    /**
     * Gets the SessionApi instance owned by this GitLabApi instance. The SessionApi is used
     * to perform a login to the GitLab API.
     *
     * @return the SessionApi instance owned by this GitLabApi instance
     */
    public SessionApi getSessionApi() {

        if (sessionApi == null) {
            synchronized (this) {
                if (sessionApi == null) {
                    sessionApi = new SessionApi(this);
                }
            }
        }

        return (sessionApi);
    }

    /**
     * Gets the SystemHooksApi instance owned by this GitLabApi instance. All methods
     * require administrator authorization.
     *
     * @return the SystemHooksApi instance owned by this GitLabApi instance
     */
    public SystemHooksApi getSystemHooksApi() {

        if (systemHooksApi == null) {
            synchronized (this) {
                if (systemHooksApi == null) {
                    systemHooksApi = new SystemHooksApi(this);
                }
            }
        }

        return (systemHooksApi);
    }

    /**
     * Gets the TagsApi instance owned by this GitLabApi instance. The TagsApi is used
     * to perform all tag and release related API calls.
     *
     * @return the TagsApi instance owned by this GitLabApi instance
     */
    public TagsApi getTagsApi() {

        if (tagsApi == null) {
            synchronized (this) {
                if (tagsApi == null) {
                    tagsApi = new TagsApi(this);
                }
            }
        }

        return (tagsApi);
    }

    /**
     * Gets the SnippetsApi instance owned by this GitLabApi instance. The SnippetsApi is used
     * to perform all snippet related API calls.
     *
     * @return the SnippetsApi instance owned by this GitLabApi instance
     */
    public SnippetsApi getSnippetApi() {
        if (snippetsApi == null) {
            synchronized (this) {
                if (snippetsApi == null) {
                    snippetsApi = new SnippetsApi(this);
                }
            }
        }

        return snippetsApi;
    }

    /**
     * Gets the TodosApi instance owned by this GitLabApi instance. The TodosApi is used to perform all Todo related API calls.
     *
     * @return the TodosApi instance owned by this GitLabApi instance
     */
    public TodosApi getTodosApi() {
        if (todosApi == null) {
            synchronized (this) {
                if (todosApi == null) {
                    todosApi = new TodosApi(this);
                }
            }
        }

        return todosApi;
    }

    /**
     * Gets the UserApi instance owned by this GitLabApi instance. The UserApi is used
     * to perform all user related API calls.
     *
     * @return the UserApi instance owned by this GitLabApi instance
     */
    public UserApi getUserApi() {

        if (userApi == null) {
            synchronized (this) {
                if (userApi == null) {
                    userApi = new UserApi(this);
                }
            }
        }

        return (userApi);
    }

    /**
     * Gets the WikisApi instance owned by this GitLabApi instance. The WikisApi is used to perform all wiki related API calls.
     *
     * @return the WikisApi instance owned by this GitLabApi instance
     */
    public WikisApi getWikisApi() {
        if (wikisApi == null) {
            synchronized (this) {
                if (wikisApi == null) {
                    wikisApi = new WikisApi(this);
                }
            }
        }

        return wikisApi;
    }

    /**
     * Specifies the version of the GitLab API to communicate with.
     */
    public enum ApiVersion {
        V3, V4;

        public String getApiNamespace() {
            return ("/api/" + name().toLowerCase());
        }
    }
}
