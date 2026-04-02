package io.github.guennhatking.libra_auction.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            
            // Tắt CSRF vì ta đã dùng JWT (Token sẽ được gửi trong header, không phải form)
            .csrf(csrf -> csrf.disable())
            
            // Quản lý phân quyền đường dẫn
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/identity/signin/", "/identity/signup/", "/identity/refresh/").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }
}