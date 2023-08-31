package org.swasth.dp.message.service.functions;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swasth.dp.message.service.task.MessageServiceConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

public class EmailDispatcher extends BaseDispatcher {

    private final Logger logger = LoggerFactory.getLogger(EmailDispatcher.class);

    public EmailDispatcher(MessageServiceConfig config) {
        super(config);
    }

    @Override
    public void processElement(Map<String, Object> event, ProcessFunction<Map<String, Object>, Map<String, Object>>.Context context, Collector<Map<String, Object>> collector) {
        try{
            System.out.println("Processing Email Event :: Mid: " + event.get("mid"));
            Map<String,Object> recipients = (Map<String,Object>) event.getOrDefault("recipients", new HashMap<>());
            if (!recipients.isEmpty()) {
                sendMail((List<String>) recipients.getOrDefault("to", new ArrayList<>()), (List<String>) recipients.getOrDefault("cc", new ArrayList<>()), (List<String>) recipients.getOrDefault("bcc", new ArrayList<>()), event.get("subject").toString(), event.get("message").toString());
                auditService.indexAudit(config.onboardIndex, config.onboardIndexAlias, eventGenerator.createMessageDispatchAudit(event, new HashMap<>()));
                System.out.println("Email is successfully sent :: Mid: " + event.get("mid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            auditService.indexAudit(config.onboardIndex, config.onboardIndexAlias, eventGenerator.createMessageDispatchAudit(event, createErrorMap("", e.getMessage(), "")));
            System.out.println("Error while sending email: " + e.getMessage());
            throw e;
        }
    }


    public Boolean sendMail(List<String> to, List<String> cc, List<String> bcc, String subject, String message){
        //compose message
        try {
            MimeMessage mimeMessage = new MimeMessage(getSession());
            for(String id: to){
                mimeMessage.addRecipient(Message.RecipientType.TO,new InternetAddress(id));
            }
            for(String id: cc){
                mimeMessage.addRecipient(Message.RecipientType.CC,new InternetAddress(id));
            }
            for(String id: bcc){
                mimeMessage.addRecipient(Message.RecipientType.BCC,new InternetAddress(id));
            }
            mimeMessage.setSubject(subject);
            mimeMessage.setContent(message, "text/html");
            //send message
            Transport.send(mimeMessage);
            return true;
        } catch (MessagingException e) {throw new RuntimeException(e);}
    }

    private Session getSession() {
        return Session.getDefaultInstance(getMailProperties(),
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.emailId, config.emailPwd);
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
