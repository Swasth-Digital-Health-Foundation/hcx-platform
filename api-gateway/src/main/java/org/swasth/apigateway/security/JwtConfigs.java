package org.swasth.apigateway.security;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix="jwt")
@ConditionalOnProperty(
        prefix = "jwt",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class JwtConfigs {

    // Valid values are : HS256, HS384, HS512, RS256, RS384, RS512, Ed25519. (see https://jwt.io).
    // HS* is for HMAC-SHA based algorithms. RS* is for RSA based signing.
    // For example, if your auth server is using HMAC-SHA256 for signing the JWTs, then use HS256.
    // If it is using RSA with SHA-512, then use RS512
    // This is an optional field. This is required only if you are using key in the config.
    private String type;

    // In case of symmetric key (i.e. HMAC based key), the key as it is. (e.g. - “abcdef…”).
    // The key must be long enough for the algorithm chosen, (e.g. for HS256 it must be at least 32 characters long).

    // In case of asymmetric keys (RSA, EdDSA etc.), only the public key, in a PEM encoded string or as a X509
    // certificate.

    // This is an optional field. You can also provide a URL to fetch JWKs from using the jwk_url field.
    private String key;

    // A URL where a provider publishes their JWKs (which are used for signing the JWTs). The URL must publish the JWKs in the standard format as described in https://tools.ietf.org/html/rfc7517.
    //This is an optional field. You can also provide the key (certificate, PEM encoded public key) as a string - in the key field along with the type.
    private String jwkUrl;

    // This is an optional field. Certain providers might set a claim which indicates the intended audience for the JWT. This can be checked by setting this field.
    //When this field is set, during the verification process of JWT, the aud claim in the JWT will be checked if it is equal to the audience field given in the configuration.
    private String audience;

    // This is an optional field. It takes a string value.
    // When this field is set, during the verification process of JWT,
    // the iss claim in the JWT will be checked if it is equal to the issuer field given in the configuration.
    private String issuer;

    // This is an optional field to provide some leeway (to account for clock skews) while comparing the JWT expiry
    // time.
    // This field expects an integer value which will be the number of seconds of the skew value.
    private String allowedSkew;

    // An optional JSON path value to the claims in the JWT token.
    // Example values are $.realm_access.roles or $.instances
    private String claimsNamespacePath;

    // Map of realms used for different entities
    private List<String> entityRealm;

}
