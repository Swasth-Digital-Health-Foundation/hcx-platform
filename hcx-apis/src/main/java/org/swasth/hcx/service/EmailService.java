package org.swasth.hcx.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Value("${email.id}")
    private String adminMail;

    @Value("${email.pwd}")
    private String adminPwd;

    @Async
    public CompletableFuture<Boolean> sendMail(String to, String subject, String message){
        try {
            MimeMessage mimeMessage = new MimeMessage(getSession());
            mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(message, "text/html");
            //send message
            Transport.send(mimeMessage);
            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {throw new RuntimeException(e);}
    }

    private Session getSession() {
        return Session.getDefaultInstance(getMailProperties(),
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(adminMail, adminPwd);
                    }
                });
    }

    private Properties getMailProperties(){
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return properties;
    }

}


