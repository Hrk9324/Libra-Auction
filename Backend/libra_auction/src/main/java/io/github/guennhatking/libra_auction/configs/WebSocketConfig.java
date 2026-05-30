package io.github.guennhatking.libra_auction.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.setErrorHandler(new CustomStompErrorHandler());

        // Native WebSocket endpoint - for wscat and Node.js WebSocket clients
        registry.addEndpoint("/auction-websocket")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new org.springframework.web.socket.server.support.DefaultHandshakeHandler());

        // SockJS fallback endpoint (for browsers without WebSocket support)
        registry.addEndpoint("/auction-websocket-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                    .setSessionCookieNeeded(true)
                    .setStreamBytesLimit(512 * 1024)
                    .setDisconnectDelay(30 * 1000)
                    .setHeartbeatTime(25 * 1000);
    }
}
