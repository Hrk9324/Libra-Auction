package io.github.guennhatking.libra_auction.filter;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import io.github.guennhatking.libra_auction.util.RSAKeyProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RSAKeyProvider rsaKeyProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && validateToken(jwt)) {
                String userId = getSubjectFromToken(jwt);
                log.info("Valid JWT token for user ID: {}", userId);

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userId, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Set authentication in SecurityContext for user: {}", userId);
            } else {
                log.warn("Invalid or missing JWT token");
            }
        } catch (Exception ex) {
            log.error("JWT filter error: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            JWSVerifier verifier = new RSASSAVerifier(rsaKeyProvider.getPublicKey());
            SignedJWT signedJWT = SignedJWT.parse(token);

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean verified = signedJWT.verify(verifier);

            if (!(verified && expiryTime.after(new Date()))) {
                log.warn("Token verification failed or expired");
                return false;
            }

            String tokenType = (String) signedJWT.getJWTClaimsSet().getClaim("type");
            if (tokenType == null || !tokenType.equals("ACCESS")) {
                log.warn("Token is not an ACCESS token");
                return false;
            }

            return true;
        } catch (Exception ex) {
            log.error("Token validation error: {}", ex.getMessage());
            return false;
        }
    }

    private String getSubjectFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (Exception ex) {
            log.error("Error getting subject from token: {}", ex.getMessage());
            return null;
        }
    }
}
