/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.accord;

import org.aoju.bus.http.Internal;
import org.aoju.bus.http.offers.CipherSuite;
import org.aoju.bus.http.secure.TlsVersion;

import javax.net.ssl.SSLSocket;
import java.util.Arrays;
import java.util.List;

/**
 * Specifies configuration for the socket connection that HTTP traffic travels through. For {@code
 * https:} URLs, this includes the TLS version and cipher suites to use when negotiating a secure
 * connection.
 *
 * <p>The TLS versions configured in a connection spec are only be used if they are also enabled in
 * the SSL socket. For example, if an SSL socket does not have TLS 1.3 enabled, it will not be used
 * even if it is present on the connection spec. The same policy also applies to cipher suites.
 *
 * <p>Use {@link Builder#allEnabledTlsVersions()} and {@link Builder#allEnabledCipherSuites} to
 * defer all feature selection to the underlying SSL socket.
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public final class ConnectionSpec {

    /**
     * Unencrypted, unauthenticated connections for {@code http:} URLs.
     */
    public static final ConnectionSpec CLEARTEXT = new Builder(false).build();
    // Most secure but generally supported list.
    private static final CipherSuite[] RESTRICTED_CIPHER_SUITES = new CipherSuite[]{
            // TLSv1.3
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_AES_128_CCM_SHA256,
            CipherSuite.TLS_AES_256_CCM_8_SHA256,

            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256
    };
    /**
     * A secure TLS connection assuming a modern client platform and server.
     */
    public static final ConnectionSpec RESTRICTED_TLS = new Builder(true)
            .cipherSuites(RESTRICTED_CIPHER_SUITES)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
            .supportsTlsExtensions(true)
            .build();
    // This is nearly equal to the cipher suites supported in Chrome 51, current as of 2016-05-25.
    // All of these suites are available on Android 7.0; earlier releases support a subset of these
    private static final CipherSuite[] APPROVED_CIPHER_SUITES = new CipherSuite[]{
            // TLSv1.3
            CipherSuite.TLS_AES_128_GCM_SHA256,
            CipherSuite.TLS_AES_256_GCM_SHA384,
            CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_AES_128_CCM_SHA256,
            CipherSuite.TLS_AES_256_CCM_8_SHA256,

            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,

            // Note that the following cipher suites are all on HTTP/2's bad cipher suites list. We'll
            // continue to include them until better suites are commonly available. For example, none
            // of the better cipher suites listed above shipped with Android 4.4 or Java 7.
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384,
            CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA,
    };
    /**
     * A modern TLS connection with extensions like SNI and ALPN available.
     */
    public static final ConnectionSpec MODERN_TLS = new Builder(true)
            .cipherSuites(APPROVED_CIPHER_SUITES)
            .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .supportsTlsExtensions(true)
            .build();
    /**
     * A backwards-compatible fallback connection for interop with obsolete servers.
     */
    public static final ConnectionSpec COMPATIBLE_TLS = new Builder(true)
            .cipherSuites(APPROVED_CIPHER_SUITES)
            .tlsVersions(TlsVersion.TLS_1_0)
            .supportsTlsExtensions(true)
            .build();
    final boolean tls;
    final boolean supportsTlsExtensions;
    final
    String[] cipherSuites;
    final
    String[] tlsVersions;

    ConnectionSpec(Builder builder) {
        this.tls = builder.tls;
        this.cipherSuites = builder.cipherSuites;
        this.tlsVersions = builder.tlsVersions;
        this.supportsTlsExtensions = builder.supportsTlsExtensions;
    }

    public boolean isTls() {
        return tls;
    }

    public List<CipherSuite> cipherSuites() {
        return cipherSuites != null ? CipherSuite.forJavaNames(cipherSuites) : null;
    }

    public List<TlsVersion> tlsVersions() {
        return tlsVersions != null ? TlsVersion.forJavaNames(tlsVersions) : null;
    }

    public boolean supportsTlsExtensions() {
        return supportsTlsExtensions;
    }

    public void apply(SSLSocket sslSocket, boolean isFallback) {
        ConnectionSpec specToApply = supportedSpec(sslSocket, isFallback);

        if (specToApply.tlsVersions != null) {
            sslSocket.setEnabledProtocols(specToApply.tlsVersions);
        }
        if (specToApply.cipherSuites != null) {
            sslSocket.setEnabledCipherSuites(specToApply.cipherSuites);
        }
    }

    private ConnectionSpec supportedSpec(SSLSocket sslSocket, boolean isFallback) {
        String[] cipherSuitesIntersection = cipherSuites != null
                ? Internal.intersect(CipherSuite.ORDER_BY_NAME, sslSocket.getEnabledCipherSuites(), cipherSuites)
                : sslSocket.getEnabledCipherSuites();
        String[] tlsVersionsIntersection = tlsVersions != null
                ? Internal.intersect(Internal.NATURAL_ORDER, sslSocket.getEnabledProtocols(), tlsVersions)
                : sslSocket.getEnabledProtocols();

        // In accordance with https://tools.ietf.org/html/draft-ietf-tls-downgrade-scsv-00
        // the SCSV cipher is added to signal that a protocol fallback has taken place.
        String[] supportedCipherSuites = sslSocket.getSupportedCipherSuites();
        int indexOfFallbackScsv = Internal.indexOf(
                CipherSuite.ORDER_BY_NAME, supportedCipherSuites, "TLS_FALLBACK_SCSV");
        if (isFallback && indexOfFallbackScsv != -1) {
            cipherSuitesIntersection = Internal.concat(
                    cipherSuitesIntersection, supportedCipherSuites[indexOfFallbackScsv]);
        }

        return new Builder(this)
                .cipherSuites(cipherSuitesIntersection)
                .tlsVersions(tlsVersionsIntersection)
                .build();
    }

    public boolean isCompatible(SSLSocket socket) {
        if (!tls) {
            return false;
        }

        if (tlsVersions != null && !Internal.nonEmptyIntersection(
                Internal.NATURAL_ORDER, tlsVersions, socket.getEnabledProtocols())) {
            return false;
        }

        if (cipherSuites != null && !Internal.nonEmptyIntersection(
                CipherSuite.ORDER_BY_NAME, cipherSuites, socket.getEnabledCipherSuites())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ConnectionSpec)) return false;
        if (other == this) return true;

        ConnectionSpec that = (ConnectionSpec) other;
        if (this.tls != that.tls) return false;

        if (tls) {
            if (!Arrays.equals(this.cipherSuites, that.cipherSuites)) return false;
            if (!Arrays.equals(this.tlsVersions, that.tlsVersions)) return false;
            if (this.supportsTlsExtensions != that.supportsTlsExtensions) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (tls) {
            result = 31 * result + Arrays.hashCode(cipherSuites);
            result = 31 * result + Arrays.hashCode(tlsVersions);
            result = 31 * result + (supportsTlsExtensions ? 0 : 1);
        }
        return result;
    }

    @Override
    public String toString() {
        if (!tls) {
            return "ConnectionSpec()";
        }

        String cipherSuitesString = cipherSuites != null ? cipherSuites().toString() : "[all enabled]";
        String tlsVersionsString = tlsVersions != null ? tlsVersions().toString() : "[all enabled]";
        return "ConnectionSpec("
                + "cipherSuites=" + cipherSuitesString
                + ", tlsVersions=" + tlsVersionsString
                + ", supportsTlsExtensions=" + supportsTlsExtensions
                + ")";
    }

    public static final class Builder {

        boolean tls;
        String[] cipherSuites;
        String[] tlsVersions;
        boolean supportsTlsExtensions;

        Builder(boolean tls) {
            this.tls = tls;
        }

        public Builder(ConnectionSpec connectionSpec) {
            this.tls = connectionSpec.tls;
            this.cipherSuites = connectionSpec.cipherSuites;
            this.tlsVersions = connectionSpec.tlsVersions;
            this.supportsTlsExtensions = connectionSpec.supportsTlsExtensions;
        }

        public Builder allEnabledCipherSuites() {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");
            this.cipherSuites = null;
            return this;
        }

        public Builder cipherSuites(CipherSuite... cipherSuites) {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");

            String[] strings = new String[cipherSuites.length];
            for (int i = 0; i < cipherSuites.length; i++) {
                strings[i] = cipherSuites[i].javaName;
            }
            return cipherSuites(strings);
        }

        public Builder cipherSuites(String... cipherSuites) {
            if (!tls) throw new IllegalStateException("no cipher suites for cleartext connections");

            if (cipherSuites.length == 0) {
                throw new IllegalArgumentException("At least one cipher suite is required");
            }

            this.cipherSuites = cipherSuites.clone(); // Defensive copy.
            return this;
        }

        public Builder allEnabledTlsVersions() {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");
            this.tlsVersions = null;
            return this;
        }

        public Builder tlsVersions(TlsVersion... tlsVersions) {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");

            String[] strings = new String[tlsVersions.length];
            for (int i = 0; i < tlsVersions.length; i++) {
                strings[i] = tlsVersions[i].javaName;
            }

            return tlsVersions(strings);
        }

        public Builder tlsVersions(String... tlsVersions) {
            if (!tls) throw new IllegalStateException("no TLS versions for cleartext connections");

            if (tlsVersions.length == 0) {
                throw new IllegalArgumentException("At least one TLS version is required");
            }

            this.tlsVersions = tlsVersions.clone(); // Defensive copy.
            return this;
        }

        public Builder supportsTlsExtensions(boolean supportsTlsExtensions) {
            if (!tls) throw new IllegalStateException("no TLS extensions for cleartext connections");
            this.supportsTlsExtensions = supportsTlsExtensions;
            return this;
        }

        public ConnectionSpec build() {
            return new ConnectionSpec(this);
        }
    }

}
