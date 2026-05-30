package io.github.guennhatking.libra_auction.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

public class CustomStompErrorHandler extends StompSubProtocolErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomStompErrorHandler.class);

    public CustomStompErrorHandler() {
        super();
    }

    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
                                              byte[] errorPayload,
                                              Throwable cause,
                                              StompHeaderAccessor clientHeaderAccessor) {
        String sessionId = clientHeaderAccessor != null ? clientHeaderAccessor.getSessionId() : "unknown";
        String destination = clientHeaderAccessor != null ? clientHeaderAccessor.getDestination() : "unknown";
        StompCommand command = clientHeaderAccessor != null ? clientHeaderAccessor.getCommand() : null;

        logger.error("STOMP error [session={}, command={}, destination={}]: {}",
                sessionId, command, destination,
                cause != null ? cause.getMessage() : "no cause");

        if (cause != null) {
            logger.debug("STOMP error stacktrace:", cause);
        }

        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(cause != null ? cause.getMessage() : "WebSocket processing error");
        errorAccessor.setSessionId(sessionId);

        String errorBody = cause != null ? cause.getMessage() : "Unknown WebSocket error";
        return MessageBuilder.createMessage(errorBody.getBytes(StandardCharsets.UTF_8), errorAccessor.getMessageHeaders());
    }
}
