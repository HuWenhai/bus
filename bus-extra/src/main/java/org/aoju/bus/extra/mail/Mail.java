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
package org.aoju.bus.extra.mail;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 邮件发送客户端
 *
 * @author Kimi Liu
 * @version 5.2.5
 * @since JDK 1.8+
 */
public class Mail {

    /**
     * 邮箱帐户信息以及一些客户端配置信息
     */
    private MailAccount mailAccount;
    /**
     * 收件人列表
     */
    private String[] tos;
    /**
     * 抄送人列表（carbon copy）
     */
    private String[] ccs;
    /**
     * 密送人列表（blind carbon copy）
     */
    private String[] bccs;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 是否为HTML
     */
    private boolean isHtml;
    /**
     * 附件列表
     */
    private DataSource[] attachments;
    /**
     * 是否使用全局会话,默认为true
     */
    private boolean useGlobalSession = true;

    /**
     * 构造,使用全局邮件帐户
     */
    public Mail() {
        this(GlobalMailAccount.INSTANCE.getAccount());
    }

    /**
     * 构造
     *
     * @param mailAccount 邮件帐户,如果为null使用默认配置文件的全局邮件配置
     */
    public Mail(MailAccount mailAccount) {
        mailAccount = (null != mailAccount) ? mailAccount : GlobalMailAccount.INSTANCE.getAccount();
        this.mailAccount = mailAccount.defaultIfEmpty();
    }

    /**
     * 创建邮件客户端
     *
     * @param mailAccount 邮件帐号
     * @return {@link Mail}
     */
    public static Mail create(MailAccount mailAccount) {
        return new Mail(mailAccount);
    }

    /**
     * 创建邮件客户端,使用全局邮件帐户
     *
     * @return {@link Mail}
     */
    public static Mail create() {
        return new Mail();
    }

    /**
     * 设置收件人
     *
     * @param tos 收件人列表
     * @return this
     * @see #setTos(String...)
     */
    public Mail to(String... tos) {
        return setTos(tos);
    }

    /**
     * 设置多个收件人
     *
     * @param tos 收件人列表
     * @return this
     */
    public Mail setTos(String... tos) {
        this.tos = tos;
        return this;
    }

    /**
     * 设置多个抄送人（carbon copy）
     *
     * @param ccs 抄送人列表
     * @return this
     */
    public Mail setCcs(String... ccs) {
        this.ccs = ccs;
        return this;
    }

    /**
     * 设置多个密送人（blind carbon copy）
     *
     * @param bccs 密送人列表
     * @return this
     */
    public Mail setBccs(String... bccs) {
        this.bccs = bccs;
        return this;
    }

    /**
     * 设置标题
     *
     * @param title 标题
     * @return this
     */
    public Mail setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置正文
     *
     * @param content 正文
     * @return this
     */
    public Mail setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 设置是否是HTML
     *
     * @param isHtml 是否为HTML
     * @return this
     */
    public Mail setHtml(boolean isHtml) {
        this.isHtml = isHtml;
        return this;
    }

    /**
     * 设置文件类型附件
     *
     * @param files 附件文件列表
     * @return this
     */
    public Mail setFiles(File... files) {
        DataSource[] attachments = new DataSource[files.length];
        for (int i = 0; i < files.length; i++) {
            attachments[i] = new FileDataSource(files[i]);
        }
        return setAttachments(attachments);
    }

    /**
     * 设置附件,附件使用{@link DataSource} 形式表示,可以使用{@link FileDataSource}包装文件表示文件附件
     *
     * @param attachments 附件列表
     * @return this
     */
    public Mail setAttachments(DataSource... attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * 设置字符集编码
     *
     * @param charset 字符集编码
     * @return this
     * @see MailAccount#setCharset(Charset)
     */
    public Mail setCharset(Charset charset) {
        this.mailAccount.setCharset(charset);
        return this;
    }

    /**
     * 设置是否使用全局会话,默认为true
     *
     * @param isUseGlobalSession 是否使用全局会话,默认为true
     * @return this
     */
    public Mail setUseGlobalSession(boolean isUseGlobalSession) {
        this.useGlobalSession = isUseGlobalSession;
        return this;
    }

    /**
     * 发送
     *
     * @return this
     * @throws InstrumentException 邮件发送异常
     */
    public Mail send() throws InstrumentException {
        try {
            return doSend();
        } catch (MessagingException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 执行发送
     *
     * @return this
     * @throws MessagingException 发送异常
     */
    private Mail doSend() throws MessagingException {
        Transport.send(buildMsg());
        return this;
    }

    /**
     * 构建消息
     *
     * @return {@link MimeMessage}消息
     * @throws MessagingException 消息异常
     */
    private MimeMessage buildMsg() throws MessagingException {
        final Charset charset = this.mailAccount.getCharset();
        final MimeMessage msg = new MimeMessage(getSession(this.useGlobalSession));
        // 发件人
        final String from = this.mailAccount.getFrom();
        if (StringUtils.isEmpty(from)) {
            // 用户未提供发送方,则从Session中自动获取
            msg.setFrom();
        } else {
            msg.setFrom(InternalMail.parseFirstAddress(from, charset));
        }
        // 标题
        msg.setSubject(this.title, charset.name());
        // 发送时间
        msg.setSentDate(new Date());
        // 内容和附件
        msg.setContent(buildContent(charset));
        // 收件人
        msg.setRecipients(MimeMessage.RecipientType.TO, InternalMail.parseAddressFromStrs(this.tos, charset));
        // 抄送人
        if (ArrayUtils.isNotEmpty(this.ccs)) {
            msg.setRecipients(MimeMessage.RecipientType.CC, InternalMail.parseAddressFromStrs(this.ccs, charset));
        }
        // 密送人
        if (ArrayUtils.isNotEmpty(this.bccs)) {
            msg.setRecipients(MimeMessage.RecipientType.BCC, InternalMail.parseAddressFromStrs(this.bccs, charset));
        }
        return msg;
    }

    /**
     * 构建邮件信息主体
     *
     * @param charset 编码
     * @return 邮件信息主体
     * @throws MessagingException 消息异常
     */
    private Multipart buildContent(Charset charset) throws MessagingException {
        final Multipart mainPart = new MimeMultipart();

        // 正文
        final BodyPart body = new MimeBodyPart();
        body.setContent(content, StringUtils.format("text/{}; charset={}", isHtml ? "html" : "plain", charset));
        mainPart.addBodyPart(body);

        // 附件
        if (ArrayUtils.isNotEmpty(this.attachments)) {
            BodyPart bodyPart;
            for (DataSource attachment : attachments) {
                bodyPart = new MimeBodyPart();
                bodyPart.setDataHandler(new DataHandler(attachment));
                bodyPart.setFileName(InternalMail.encodeText(attachment.getName(), charset));
                mainPart.addBodyPart(bodyPart);
            }
        }

        return mainPart;
    }

    /**
     * 获取默认邮件会话
     * 如果为全局单例的会话,则全局只允许一个邮件帐号,否则每次发送邮件会新建一个新的会话
     *
     * @param isSingleton 是否使用单例Session
     * @return 邮件会话 {@link Session}
     */
    private Session getSession(boolean isSingleton) {
        final MailAccount mailAccount = this.mailAccount;
        Authenticator authenticator = null;
        if (mailAccount.isAuth()) {
            authenticator = new UserPassAuthenticator(mailAccount.getUser(), mailAccount.getPass());
        }

        return isSingleton ? Session.getDefaultInstance(mailAccount.getSmtpProps(), authenticator) //
                : Session.getInstance(mailAccount.getSmtpProps(), authenticator);
    }

}
