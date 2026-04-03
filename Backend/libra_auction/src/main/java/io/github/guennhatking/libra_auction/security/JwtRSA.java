package io.github.guennhatking.libra_auction.security;

import io.github.guennhatking.libra_auction.enums.TokenType;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtRSA {
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String JWT_FORMAT = encodeHeader() + ".%s.%s";

    private final JwtUtils jwtUtils;

    public JwtRSA(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    private static String encodeHeader() {
        String headerJson = "{\"typ\":\"JWT\",\"alg\":\"RS256\"}";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes());
    }

    public String createToken(String userId, TokenType type, Long expirationTime) throws Exception {
        long issuedAtTime = System.currentTimeMillis();
        long expiryTime = issuedAtTime + expirationTime;

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", userId);
        claimsMap.put("type", type.toString());
        claimsMap.put("iat", issuedAtTime / 1000);
        claimsMap.put("exp", expiryTime / 1000);

        String payload = encodePayload(claimsMap);
        String signature = signPayload(payload);

        return String.format(JWT_FORMAT, payload, signature);
    }

    public boolean verifyToken(String token, PublicKey publicKey) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String payload = parts[1];
        String signature = parts[2];

        byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update((parts[0] + "." + payload).getBytes());

        return sig.verify(signatureBytes);
    }

    public String extractClaim(String token, String claimName) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        int startIndex = payload.indexOf("\"" + claimName + "\":\"");
        if (startIndex == -1) {
            startIndex = payload.indexOf("\"" + claimName + "\":");
            if (startIndex == -1) {
                return null;
            }
            startIndex += claimName.length() + 3;
            int endIndex = payload.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = payload.indexOf("}", startIndex);
            }
            return payload.substring(startIndex, endIndex).trim();
        }

        startIndex += claimName.length() + 4;
        int endIndex = payload.indexOf("\"", startIndex);
        return payload.substring(startIndex, endIndex);
    }

    public Long getExpirationTime(String token) throws Exception {
        String exp = extractClaim(token, "exp");
        if (exp == null) {
            return null;
        }
        return Long.parseLong(exp) * 1000;
    }

    private String encodePayload(Map<String, Object> claims) throws Exception {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");

        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.toString().getBytes());
    }

    private String signPayload(String payload) throws Exception {
        PrivateKey privateKey = jwtUtils.getPrivateKey();
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(privateKey);

        String headerAndPayload = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9." + payload;
        sig.update(headerAndPayload.getBytes());

        byte[] signatureBytes = sig.sign();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}
