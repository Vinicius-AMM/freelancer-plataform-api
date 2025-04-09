package com.manager.freelancer_management_api.infra.security;

import com.manager.freelancer_management_api.domain.exceptions.PrivateKeyLoadException;
import com.manager.freelancer_management_api.domain.exceptions.PublicKeyLoadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class KeyConfig {

    @Value("${jwt.public.key}")
    private Resource publicKeyResource;

    @Value("${jwt.private.key}")
    private Resource privateKeyResource;

    @Bean
    public RSAPublicKey publicKey() throws Exception {
        try{
            String publicKeyContent = new String(publicKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String publicKeyPEM = cleanKey(publicKeyContent, "PUBLIC");

            byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (Exception e){
            throw new PublicKeyLoadException("Error loading public key");
        }
    }

    @Bean
    public RSAPrivateKey privateKey() throws Exception {
        try{
            String privateKeyContent = new String(privateKeyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String privateKeyPEM = cleanKey(privateKeyContent, "PRIVATE");

            byte[] decodedKey = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception e){
            throw new PrivateKeyLoadException("Error loading private key");
        }
    }

    private String cleanKey(String keyContent, String keyType) {
        return keyContent
                .replace("-----BEGIN " + keyType + " KEY-----", "")
                .replace("-----END " + keyType + " KEY-----", "")
                .replaceAll("\\s", "");
    }
}