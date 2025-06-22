package com.example.backend_chat.config;

import com.example.backend_chat.service.CustomUserDetailsService;
import com.example.backend_chat.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// validate jwt
//
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    @Autowired
    public JwtChannelInterceptor(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override

    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            // Check query parameter for token
            String queryToken = accessor.getNativeHeader("token") != null
                    ? accessor.getNativeHeader("token").get(0)
                    : null;
            if (token == null && queryToken != null) {
                token = queryToken.startsWith("Bearer ") ? queryToken.substring(7) : queryToken;
            }

            logger.debug("Processing WebSocket CONNECT with token: {}", token != null ? "present" : "null");
            if (token != null) {
                try {
                    String email = jwtUtil.extractEmail(token);
                    UserDetails user = userDetailsService.loadUserByUsername(email);
                    if (jwtUtil.isTokenValid(token, user)) {
                        logger.info("WebSocket authentication successful for user: {}", email);
                        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                    } else {
                        logger.error("Invalid or expired token for WebSocket connection");
                        return buildError("Invalid or expired token");
                    }
                } catch (Exception e) {
                    logger.error("Authentication error: {}", e.getMessage(), e);
                    return buildError("Authentication error: " + e.getMessage());
                }
            } else {
                logger.error("Missing Authorization header or token query parameter for WebSocket connection");
                return buildError("Missing Authorization header or token");
            }
        }
        return message;
    }
    private String extractToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }
    private Message<byte[]> buildError(String message) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(message);
        return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
    }
}
