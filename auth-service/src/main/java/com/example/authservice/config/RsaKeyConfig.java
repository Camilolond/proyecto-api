package com.example.authservice.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

@Configuration
public class RsaKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyConfig.class);

    /**
     * RSA key pair used to sign the JWTs with RS256. The private key is read from a PEM file (PKCS#8) and the
     * public key is derived from it. If the file doesnt exist a temporary key pair is generated, which is fine
     * for local development but the tokens stop working every time the service restarts
     * @param privateKeyPath Path to the private key PEM file
     */
    @Bean
    public RSAKey rsaKey(@Value("${jwt.private-key-path:}") String privateKeyPath) throws Exception {
        if (privateKeyPath.isBlank() || !Files.exists(Path.of(privateKeyPath))) {
            log.warn("No RSA private key found at '{}', generating a temporary key pair", privateKeyPath);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return buildKey((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
        }
        RSAPrivateKey privateKey;
        try (InputStream in = Files.newInputStream(Path.of(privateKeyPath))) {
            privateKey = RsaKeyConverters.pkcs8().convert(in);
        }
        RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey) privateKey;
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new RSAPublicKeySpec(crtKey.getModulus(), crtKey.getPublicExponent()));
        log.info("Loaded RSA private key from '{}'", privateKeyPath);
        return buildKey(publicKey, privateKey);
    }

    /**
     * The key ID (kid) is the thumbprint of the public key, so it stays the same while the key doesnt change
     */
    private RSAKey buildKey(RSAPublicKey publicKey, RSAPrivateKey privateKey) throws JOSEException {
        RSAKey key = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
        return new RSAKey.Builder(key).keyID(key.computeThumbprint().toString()).build();
    }
}
