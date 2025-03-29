package com.kyut.ordo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Префікс для точок призначення, де клієнти можуть підписуватися для отримання повідомлень
        config.enableSimpleBroker("/topic");
        
        // Префікс для точок призначення, куди клієнти можуть надсилати повідомлення
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Реєстрація кінцевої точки для підключення клієнтів
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Дозволяємо підключення з будь-якого джерела (для розробки)
                .withSockJS(); // Використовуємо SockJS для підтримки старіших браузерів
    }
}
