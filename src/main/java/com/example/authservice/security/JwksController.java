package com.example.authservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class JwksController {
    private final JwtService jwt;

    @GetMapping(path = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> jwks() {
        RSAPublicKey p = jwt.getPublicKey();
        return Map.of("keys", new Object[]{Map.of("kty", "RSA", "use", "sig", "kid", jwt.getKid(), "alg", "RS256", "n", b64(p.getModulus()), "e", b64(p.getPublicExponent()))});
    }

    private String b64(BigInteger i) {
        byte[] b = i.toByteArray();
        if (b.length > 1 && b[0] == 0) b = java.util.Arrays.copyOfRange(b, 1, b.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
