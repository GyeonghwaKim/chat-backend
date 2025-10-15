package com.example.chat.controller;

import com.example.chat.model.ChatMessage;
import com.example.chat.model.MessageType;
import com.example.chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 채팅 컨트롤러
 * WebSocket 메시지 처리 및 REST API 제공
 */
@Controller
public class ChatController {

    /** WebSocket 메시지 전송을 위한 템플릿 */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /** 채팅 비즈니스 로직 처리 서비스 */
    @Autowired
    private ChatService chatService;

    /**
     * 채팅 메시지 전송 처리
     * WebSocket 경로: /app/chat.send
     * @param message 전송할 채팅 메시지
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        // 메시지 ID 및 타임스탬프 설정
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setType(MessageType.CHAT);

        // 메시지 저장
        chatService.saveMessage(message);

        // 수신자에게 메시지 전송 (/queue/messages/{receiverId})
        messagingTemplate.convertAndSend(
            "/queue/messages/" + message.getReceiver(),
            message
        );

        // 발신자에게도 메시지 전송 (본인 화면에 표시하기 위함)
        messagingTemplate.convertAndSend(
            "/queue/messages/" + message.getSender(),
            message
        );
    }

    /**
     * 사용자 접속 처리
     * WebSocket 경로: /app/chat.join
     * @param message 접속 메시지 (sender 필드에 사용자 ID 포함)
     * @param headerAccessor WebSocket 세션 헤더 정보
     */
    @MessageMapping("/chat.join")
    public void joinUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setType(MessageType.JOIN);

        // 세션 ID 가져오기
        String sessionId = headerAccessor.getSessionId();

        // 온라인 사용자 목록에 추가 (세션 ID 포함)
        chatService.addUser(message.getSender(), message.getSender(), sessionId);

        // 전체 사용자에게 업데이트된 온라인 사용자 목록 브로드캐스트
        messagingTemplate.convertAndSend("/topic/users", chatService.getOnlineUsers());
    }

    /**
     * 사용자 퇴장 처리
     * WebSocket 경로: /app/chat.leave
     * @param message 퇴장 메시지 (sender 필드에 사용자 ID 포함)
     */
    @MessageMapping("/chat.leave")
    public void leaveUser(@Payload ChatMessage message) {
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setType(MessageType.LEAVE);

        // 온라인 사용자 목록에서 제거
        chatService.removeUser(message.getSender());

        // 전체 사용자에게 업데이트된 온라인 사용자 목록 브로드캐스트
        messagingTemplate.convertAndSend("/topic/users", chatService.getOnlineUsers());
    }

    /**
     * REST API: 온라인 사용자 목록 조회
     * @return 온라인 사용자 맵 (userId -> username)
     */
    @GetMapping("/api/users/online")
    @ResponseBody
    public Map<String, String> getOnlineUsers() {
        return chatService.getOnlineUsers();
    }

    /**
     * REST API: 특정 사용자의 채팅 히스토리 조회
     * @param userId 사용자 ID
     * @return 채팅 메시지 리스트
     */
    @GetMapping("/api/chat/history/{userId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable String userId) {
        return chatService.getChatHistory(userId);
    }

    /**
     * REST API: 두 사용자 간 1:1 대화 히스토리 조회
     * @param userId1 사용자1 ID
     * @param userId2 사용자2 ID
     * @return 두 사용자 간 채팅 메시지 리스트
     */
    @GetMapping("/api/chat/history/{userId1}/{userId2}")
    @ResponseBody
    public List<ChatMessage> getChatHistoryBetween(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        return chatService.getChatHistoryBetween(userId1, userId2);
    }
}
