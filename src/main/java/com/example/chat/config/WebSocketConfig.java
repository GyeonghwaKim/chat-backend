package com.example.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * STOMP 프로토콜을 사용한 WebSocket 메시지 브로커 설정
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String[] allowedOrigins;

    /**
     * 메시지 브로커 설정
     * - /topic: 1:N 브로드캐스트용
     * - /queue: 1:1 개인 메시지용
     * - /app: 클라이언트에서 서버로 메시지 전송 시 사용할 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정 - /topic, /queue로 시작하는 메시지를 브로커가 처리
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 메시지 전송 시 /app으로 시작하는 경로 사용
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록
     * - /ws: WebSocket 연결 엔드포인트
     * - SockJS 폴백 옵션 활성화
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins)  // 환경변수로 설정된 origin만 허용
                .withSockJS();  // SockJS 폴백 활성화
    }
}
