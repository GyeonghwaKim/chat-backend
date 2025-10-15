package com.example.chat.config;

import com.example.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 이벤트 리스너
 * 연결/해제 이벤트를 감지하여 사용자 상태 관리
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket 연결 이벤트 처리
     * 클라이언트가 WebSocket에 연결될 때 호출
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        logger.info("새로운 WebSocket 연결: sessionId={}", sessionId);
    }

    /**
     * WebSocket 연결 해제 이벤트 처리
     * 클라이언트가 연결을 끊거나 네트워크가 끊겼을 때 호출
     * 정상 종료, 비정상 종료 모두 처리
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // 세션에서 사용자 ID 조회
        String userId = chatService.getUserIdBySessionId(sessionId);

        if (userId != null) {
            logger.info("사용자 연결 해제: userId={}, sessionId={}", userId, sessionId);

            // 사용자 제거
            chatService.removeUser(userId);
            chatService.removeSession(sessionId);

            // 업데이트된 온라인 사용자 목록 브로드캐스트
            messagingTemplate.convertAndSend("/topic/users", chatService.getOnlineUsers());

            logger.info("사용자가 제거되었습니다: {}", userId);
        } else {
            logger.warn("연결 해제 이벤트에서 사용자를 찾을 수 없음: sessionId={}", sessionId);
        }
    }
}
