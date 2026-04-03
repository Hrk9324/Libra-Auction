package io.github.guennhatking.libra_auction.services;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class GoogleOAuthService {
    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.redirect-uri}")
    private String redirectUri;

    private static final String GOOGLE_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleOAuthService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public GoogleUserInfo exchangeCodeForUserInfo(String code) throws Exception {
        String accessToken = exchangeCodeForToken(code);
        return getUserInfoFromGoogle(accessToken);
    }

    private String exchangeCodeForToken(String code) throws Exception {
        String requestBody = String.format(
            "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
            code, clientId, clientSecret, redirectUri
        );
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            GOOGLE_TOKEN_ENDPOINT,
            HttpMethod.POST,
            entity,
            String.class
        );

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("access_token").asString();
    }

    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            GOOGLE_USERINFO_ENDPOINT,
            HttpMethod.GET,
            entity,
            String.class
        );

        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        return new GoogleUserInfo(
            jsonNode.get("sub").asString(),
            jsonNode.get("email").asString(),
            jsonNode.get("name").asString(),
            jsonNode.get("picture").asString()
        );
    }

    public String extractUserIdFromIdToken(String idToken) throws Exception {
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid ID token");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        JsonNode jsonNode = objectMapper.readTree(payload);
        return jsonNode.get("sub").asString();
    }
}
