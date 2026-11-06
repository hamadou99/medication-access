package ch.unige.mssn.pmp.demo.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Fournisseur d'identité de test : publie une clé publique et signe de vrais jetons, valides ou volontairement faux. */
public final class TestTokens {

    public static final String ISSUER = "http://test.invalid/realms/pmp";
    public static final String AUDIENCE = "pmp-api";

    private static final RSAKey KEY = generate("cle-test");
    private static final RSAKey FOREIGN_KEY = generate("cle-etrangere");
    private static final String JWKS_URI = startJwksServer();

    private TestTokens() {
    }

    public static String jwksUri() {
        return JWKS_URI;
    }

    public static String forPersona(Personas.Persona persona) {
        return sign(KEY, claims(persona).build());
    }

    public static String modified(Personas.Persona persona, Consumer<JWTClaimsSet.Builder> change) {
        JWTClaimsSet.Builder builder = claims(persona);
        change.accept(builder);
        return sign(KEY, builder.build());
    }

    public static String signedWithForeignKey(Personas.Persona persona) {
        return sign(FOREIGN_KEY, claims(persona).build());
    }

    private static JWTClaimsSet.Builder claims(Personas.Persona persona) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .subject(persona.personId())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(Duration.ofHours(1))))
                .claim("pmp_person_id", persona.personId())
                .claim("name", persona.displayName())
                .claim("realm_access", Map.of("roles", List.copyOf(persona.roles())));
        if (persona.patientId() != null) {
            builder.claim("epr_spid", persona.patientId());
        }
        return builder;
    }

    private static String sign(RSAKey key, JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RSAKey generate(String id) {
        try {
            return new RSAKeyGenerator(2048).keyID(id).keyUse(KeyUse.SIGNATURE).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String startJwksServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
            server.createContext("/certs", exchange -> {
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            return "http://127.0.0.1:" + server.getAddress().getPort() + "/certs";
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
