package org.swasth.apigateway.filters;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.swasth.apigateway.exception.ClientException;
import org.swasth.apigateway.exception.ErrorCodes;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class CheckSessions {
    private static final Logger logger = LoggerFactory.getLogger(CheckSessions.class);
    private Keycloak keycloak;
    @Value("${keycloak.base-url}")
    private String keycloakURL;
    @Value("${keycloak.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.admin-user}")
    private String keycloakAdminUserName;
    @Value("${keycloak.master-realm}")
    private String keycloakMasterRealm;
    @Value("${keycloak.participant-realm}")
    private String keycloackParticipantRealm;
    @Value("${keycloak.client-id}")
    private String keycloackClientId;

    @PostConstruct()
    public void init() {
        keycloak = Keycloak.getInstance(keycloakURL, keycloakMasterRealm, keycloakAdminUserName, keycloakAdminPassword, keycloackClientId);
    }

    public void checkSessions(String subject, String sessionId) throws ClientException {
        try {
            RealmResource realmResource = keycloak.realm(keycloackParticipantRealm);
            UserResource usersResource = realmResource.users().get(subject);
            List<UserSessionRepresentation> activeSessions = usersResource.getUserSessions();
            if (!activeSessions.isEmpty()) {
                if (activeSessions.stream().anyMatch(session -> session.getId().equals(sessionId)));
                    logger.info("The user has active session");
            } else {
                List<UserSessionRepresentation> offlineSessions = usersResource.getOfflineSessions(sessionId);
                if (offlineSessions.stream().anyMatch(session -> session.getId().equals(sessionId)))
                    logger.info("The user has offline session");
            }
        } catch (Exception notFoundException) {
            throw new ClientException(ErrorCodes.ERR_ACCESS_DENIED, "The user is offline or inactive");
        }
    }
}
