package io.github.guennhatking.libra_auction.util;

import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAKeyProvider {
    
    @Value("${jwt.private-key}")
    private String privateKeyPath;
    
    @Value("${jwt.public-key}")
    private String publicKeyPath;
    
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    
    public RSAPrivateKey getPrivateKey() throws JOSEException {
        if (privateKey == null) {
            try {
                String keyString = new String(Files.readAllBytes(Paths.get(privateKeyPath)), StandardCharsets.UTF_8);
                byte[] decodedKey = Base64.getDecoder().decode(keyString);
                
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                privateKey = (RSAPrivateKey) kf.generatePrivate(spec);
            } catch (Exception e) {
                throw new JOSEException("Failed to load private key", e);
            }
        }
        return privateKey;
    }
    
    public RSAPublicKey getPublicKey() throws JOSEException {
        if (publicKey == null) {
            try {
                String keyString = new String(Files.readAllBytes(Paths.get(publicKeyPath)), StandardCharsets.UTF_8);
                byte[] decodedKey = Base64.getDecoder().decode(keyString);
                
                X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                publicKey = (RSAPublicKey) kf.generatePublic(spec);
            } catch (Exception e) {
                throw new JOSEException("Failed to load public key", e);
            }
        }
        return publicKey;
    }
}
