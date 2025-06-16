package com.example.backend_chat.config;

import com.example.backend_chat.service.CustomUserDetailsService;
import com.example.backend_chat.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

// work before full establish the session
// purpose extract the token an establish the auth in session
public class AuthHandshakeInterceptor implements HandshakeInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthHandshakeInterceptor( JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService ) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void afterHandshake( ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception ) {
        if (exception != null) {
            logger.error("Handshake failed: {}", exception.getMessage());
        } else {
            logger.info("WebSocket handshake completed successfully");
        }
    }

    //     Extract and validate token once before the socket connection is accepted.
    @Override
    public boolean beforeHandshake( ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes ) {
        // goal extract the token for header
        // processes the initial WebSocket handshake request
        // store user Authentication info into the session (in attributes)
        // Extract token from headers or query params
        String token = null;

        // Example 1: From query params: ws://localhost:8080/ws?token=xxx
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
            var httpServletRequest = servletRequest.getServletRequest();
            token = httpServletRequest.getParameter("token");
        }

        // Example 2: From Authorization header
        if (token == null && request.getHeaders().containsKey("Authorization")) {
            token = request.getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }
        if (token != null) {
            try {
                String email = jwtUtil.extractEmail(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                if (jwtUtil.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    attributes.put("auth", authentication);
                    logger.info("WebSocket handshake authenticated for user: {}", email);
                    return true;
                } else {
                    logger.warn("Invalid or expired JWT token");
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
            } catch (io.jsonwebtoken.JwtException e) {
                logger.error("JWT processing error: {}", e.getMessage());
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            } catch (Exception e) {
                logger.error("Unexpected error during handshake authentication: {}", e.getMessage());
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return false;
            }
        } else {
            logger.warn("JWT Token is missing from both Authorization header and query parameters");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }


}
