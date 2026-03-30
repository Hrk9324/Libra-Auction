package io.github.guennhatking.libra_auction.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class RSAKeyGenerator {
    
    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        
        String resourcePath = "Backend/libra_auction/src/main/resources";
        
        Files.writeString(Paths.get(resourcePath + "/private_key.txt"), privateKey);
        Files.writeString(Paths.get(resourcePath + "/public_key.txt"), publicKey);
        
        System.out.println("Keys generated successfully!");
        System.out.println("\nPrivate Key:");
        System.out.println(privateKey);
        System.out.println("\n\nPublic Key:");
        System.out.println(publicKey);
    }
}
