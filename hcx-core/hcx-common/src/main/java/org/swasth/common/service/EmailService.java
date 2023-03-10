package org.swasth.common.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailService {

      public CompletableFuture<Boolean> sendMail(String to, String subject, String message,String adminMail,String adminPwd){
          System.out.println("message" + message);

          //Get properties object
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.starttls.required", "true");
            properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            //get Session
            Session session = Session.getDefaultInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(adminMail,adminPwd);
                        }
                    });
            //compose message
            try {
                MimeMessage mimeMessage = new MimeMessage(session);
                mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
                mimeMessage.setSubject(subject);
                mimeMessage.setContent(message, "text/html");

                //send message
                Transport.send(mimeMessage);
                return CompletableFuture.completedFuture(true);
            } catch (MessagingException e) {throw new RuntimeException(e);}
        }
    }

