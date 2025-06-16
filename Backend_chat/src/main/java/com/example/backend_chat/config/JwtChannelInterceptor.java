package com.example.backend_chat.config;

import com.example.backend_chat.service.CustomUserDetailsService;
import com.example.backend_chat.utils.JwtUtil;
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

        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            if (token != null) {
                try {
                    String email = jwtUtil.extractEmail(token);
                    UserDetails user = userDetailsService.loadUserByUsername(email);
                    if (jwtUtil.isTokenValid(token, user)) {
                        accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                    } else {
                        return buildError("Invalid or expired token");
                    }
                } catch (Exception e) {
                    return buildError("Authentication error: " + e.getMessage());
                }
            } else {
                return buildError("Missing Authorization header");
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
