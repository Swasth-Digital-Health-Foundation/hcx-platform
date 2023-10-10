package org.swasth.hcx.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swasth.common.exception.ClientException;
import org.swasth.common.helpers.EventGenerator;
import org.swasth.kafka.client.KafkaClient;
import org.swasth.postgresql.IDatabaseService;

import javax.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.swasth.common.utils.Constants.EMAIL;

@Service
public class KeycloakApiAccessService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakApiAccessService.class);
    @Value("${keycloak.base-url}")
    private String keycloakURL;
    @Value("${keycloak.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.admin-user}")
    private String keycloakAdminUserName;
    @Value("${keycloak.master-realm}")
    private String keycloakMasterRealm;
    @Value("${postgres.api-access-secrets-expiry-table}")
    private String apiAccessTable;
    @Value("${secret.expiry-time}")
    private int secretExpiry;
    @Value("${keycloak.protocol-access-realm}")
    private String keycloackProtocolAccessRealm;
    @Value("${keycloak.admin-client-id}")
    private String keycloackClientId;
    @Value("${email.user-token-message}")
    private String userEmailMessage;
    @Value("${email.user-token-subject}")
    private String emailSub;
    @Value("${kafka.topic.message}")
    private String messageTopic;
    @Autowired
    private KafkaClient kafkaClient;
    @Autowired
    private IDatabaseService postgreSQLClient;
    @Autowired
    protected EventGenerator eventGenerator;

    public void addUserWithParticipant(String email, String participantCode, String name) throws ClientException {
        Response response = null;
        try (Keycloak keycloak = Keycloak.getInstance(keycloakURL, keycloakMasterRealm, keycloakAdminUserName, keycloakAdminPassword, keycloackClientId)) {
            RealmResource realmResource = keycloak.realm(keycloackProtocolAccessRealm);
            UsersResource usersResource = realmResource.users();
            String userName = String.format("%s:%s", participantCode, email);
            List<UserRepresentation> existingUsers = usersResource.search(userName);
            if (!existingUsers.isEmpty()) {
                logger.info("user name  : {} is already exists", userName);
            } else {
                String password = generateRandomPassword();
                UserRepresentation user = createUserRequest(userName, name, password);
                response = usersResource.create(user);
                response.close();
                if (response.getStatus() == 201) {
                    String query = String.format("INSERT INTO %s (user_id,participant_code,secret_generation_date,secret_expiry_date,username)VALUES ('%s','%s',%d,%d,'%s');", apiAccessTable, email,
                            participantCode, System.currentTimeMillis(), System.currentTimeMillis() + (secretExpiry * 24 * 60 * 60 * 1000), userName);
                    postgreSQLClient.execute(query);
                    String message = userEmailMessage;
                    message = message.replace("NAME", name).replace("USER_ID", email).replace("PASSWORD", password).replace("PARTICIPANT_CODE", participantCode);
                    kafkaClient.send(messageTopic, EMAIL, eventGenerator.getEmailMessageEvent(message, emailSub, List.of(email), new ArrayList<>(), new ArrayList<>()));
                }
            }
        } catch (Exception e) {
            throw new ClientException("Unable to add user and participant record to Keycloak: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private UserRepresentation createUserRequest(String userName, String name, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userName);
        user.setFirstName(name);
        user.setEnabled(true);
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("entity", List.of("api-access"));
        user.setAttributes(attributes);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return user;
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#&*";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder password = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            password.append(characters.charAt(randomIndex));
        }
        return password.toString();
    }


    public void removeUserWithParticipant(String participantCode, String email) throws ClientException {
        try (Keycloak keycloak = Keycloak.getInstance(keycloakURL, keycloakMasterRealm, keycloakAdminUserName, keycloakAdminPassword, keycloackClientId)) {
            String userName = String.format("%s:%s", participantCode, email);
            RealmResource realmResource = keycloak.realm(keycloackProtocolAccessRealm);
            UsersResource usersResource = realmResource.users();
            List<UserRepresentation> existingUsers = usersResource.search(userName);
            if (existingUsers.isEmpty()) {
                return;
            }
            String userId = existingUsers.get(0).getId();
            usersResource.get(userId).remove();
        } catch (Exception e) {
            throw new ClientException(e.getMessage());
        }
    }

}
