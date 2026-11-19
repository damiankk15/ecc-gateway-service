package com.ecc.gateway.integration.support.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public final class TestJwtIssuer {

    public static final String ISSUER_URI_V = "https://keycloak.test/realms/ecc-realm";
    public static final String JWKS_PATH_V = "/realms/ecc-realm/protocol/openid-connect/certs";

    private static final String FOREIGN_ISSUER_URI_V = "https://keycloak.evil.test/realms/ecc-realm";
    private static final String KEY_ID_V = "test-key";
    private static final String SUBJECT_V = "test-user";

    private static final RSAKey SIGNING_KEY = generateKey();
    private static final RSAKey FOREIGN_KEY = generateKey();

    private TestJwtIssuer() {}

    public static String jwkSetJson() {
        return new JWKSet(SIGNING_KEY.toPublicJWK()).toString();
    }

    public static String validToken() {
        return sign(SIGNING_KEY, ISSUER_URI_V, Instant.now().plus(Duration.ofMinutes(5)));
    }

    public static String expiredToken() {
        return sign(SIGNING_KEY, ISSUER_URI_V, Instant.now().minus(Duration.ofMinutes(5)));
    }

    public static String foreignIssuerToken() {
        return sign(SIGNING_KEY, FOREIGN_ISSUER_URI_V, Instant.now().plus(Duration.ofMinutes(5)));
    }

    public static String foreignKeyToken() {
        return sign(FOREIGN_KEY, ISSUER_URI_V, Instant.now().plus(Duration.ofMinutes(5)));
    }

    private static String sign(RSAKey key, String issuer, Instant expiresAt) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject(SUBJECT_V)
            .issuer(issuer)
            .issueTime(Date.from(expiresAt.minus(Duration.ofMinutes(10))))
            .expirationTime(Date.from(expiresAt))
            .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID_V).type(JOSEObjectType.JWT).build();
        SignedJWT jwt = new SignedJWT(header, claims);

        try {
            jwt.sign(new RSASSASigner(key));
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign test JWT", e);
        }

        return jwt.serialize();
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048).keyID(KEY_ID_V).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not generate test RSA key", e);
        }
    }
}
