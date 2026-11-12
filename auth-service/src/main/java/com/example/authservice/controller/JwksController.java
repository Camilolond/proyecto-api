package com.example.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwksController {

    private final JWKSet publicKeys;

    /**
     * toPublicJWK() removes the private part of the key, only the public key is ever exposed
     */
    public JwksController(RSAKey rsaKey) {
        this.publicKeys = new JWKSet(rsaKey.toPublicJWK());
    }

    /**
     * Responds to a GET /.well-known/jwks.json call, product-service and user-service download the
     * public key from here to verify the signature of the tokens
     * @return The public key in JWK Set format (RFC 7517)
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        return publicKeys.toJSONObject();
    }
}
