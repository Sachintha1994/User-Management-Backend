package com.example.authservice.security;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class JwtService {
    @Value("${app.jwt.iss}")
    private String issuer;
    @Value("${app.jwt.aud}")
    private String audience;
    @Value("${app.jwt.accessMinutes:15}")
    private long accessMinutes;
    @Value("${app.jwt.privatePem:}")
    private String privatePem;
    @Value("${app.jwt.publicPem:}")
    private String publicPem;
    private RSAPrivateKey privateKey;
    @Getter
    private RSAPublicKey publicKey;
    @Getter
    private String kid;

    @PostConstruct
    public void init() throws Exception {
        if (privatePem != null && !privatePem.isBlank() && publicPem != null && !publicPem.isBlank()) {
            privateKey = (RSAPrivateKey) readPrivate(privatePem);
            publicKey = (RSAPublicKey) readPublic(publicPem);
        } else {
            var kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            var kp = kpg.generateKeyPair();
            privateKey = (RSAPrivateKey) kp.getPrivate();
            publicKey = (RSAPublicKey) kp.getPublic();
            System.out.println("[WARN] Ephemeral RSA keypair generated for JWT. Provide APP_JWT_PRIVATE_PEM/APP_JWT_PUBLIC_PEM for production.");
        }
        kid = java.util.UUID.randomUUID().toString();
    }

    public String buildAccessToken(String sub, java.util.Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder().setHeaderParam("kid", kid).setIssuer(issuer).setAudience(audience).setSubject(sub).setIssuedAt(Date.from(now)).setExpiration(Date.from(now.plus(accessMinutes, ChronoUnit.MINUTES))).addClaims(claims).signWith(privateKey, SignatureAlgorithm.RS256).compact();
    }

    public String extractUsername(String token) {
        return parser().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private JwtParser parser() {
        return Jwts.parserBuilder().requireIssuer(issuer).requireAudience(audience).setSigningKey(publicKey).build();
    }

    private PrivateKey readPrivate(String pem) throws Exception {
        String n = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\s", "");
        byte[] b = Base64.getDecoder().decode(n);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b));
    }

    private PublicKey readPublic(String pem) throws Exception {
        String n = pem.replaceAll("-----BEGIN (.*)-----", "").replaceAll("-----END (.*)-----", "").replaceAll("\s", "");
        byte[] b = Base64.getDecoder().decode(n);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(b));
    }

}
