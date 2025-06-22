package com.example.backend_chat.config;

import com.example.backend_chat.service.CustomUserDetailsService;
import com.example.backend_chat.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtChannelInterceptor jwtChannelInterceptor;
    @Autowired
    public WebSocketConfig( JwtChannelInterceptor jwtChannelInterceptor, CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil ) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*");            // for JavaFX

        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor(jwtUtil,customUserDetailsService))
                .setAllowedOriginPatterns("*")
//                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors( jwtChannelInterceptor);
    }
}
