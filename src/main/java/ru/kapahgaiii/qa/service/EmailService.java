package ru.kapahgaiii.qa.service;

import org.springframework.beans.factory.annotation.Value;
import ru.kapahgaiii.qa.domain.RestorePassword;
import ru.kapahgaiii.qa.domain.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@org.springframework.stereotype.Service("EmailService")
public class EmailService {

    @Value("${domain}")
    private String domain;

    @Value("${gmail}")
    private String username;

    @Value("${gmailPassword}")
    private String password;

    private Properties props;

    public EmailService() {
        props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
    }

    public void send(String subject, String text, String toEmail) {
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            //от кого
            message.setFrom(new InternetAddress(username, "ыыыы.рф"));
            //кому
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            //тема сообщения
            message.setSubject(subject);
            //текст
            message.setContent(text, "text/html; charset=utf-8");

            //отправляем сообщение
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPasswordHash(User user, RestorePassword restorePassword) {
        StringBuilder html = new StringBuilder();
        html.append("<span style='margin: 10px; display: block;'>Кто-то, возможно Вы, воспользовался услугой ");
        html.append("восстановления пароля на сайте <b>");
        html.append(domain);
        html.append("</b></span>");
        html.append("<a style='margin: 10px; display: block;' href='http://");
        html.append(domain);
        html.append("/restore_password_login?hash=");
        html.append(restorePassword.getHash());
        html.append("'>Если это были Вы, пройдите по этой ссылке.</a>");
        html.append("<span style='margin: 10px; display: block;'>Если это были не Вы, просто проигнорируйте это письмо. ");
        html.append("Ссылка будет активна в течении часа или до первого перехода по ней.");
        send("Восстановление пароля", html.toString(), user.getEmail());
    }
}