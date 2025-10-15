package com.example.chat.service;

import com.example.chat.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 채팅 서비스 클래스
 * 메모리 기반으로 사용자 및 메시지 관리
 */
@Service
public class ChatService {

    /** 온라인 사용자 목록 (userId -> username) */
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    /** 채팅 히스토리 저장소 (userId -> 메시지 리스트) */
    private final Map<String, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();

    /** 세션 ID -> 사용자 ID 매핑 */
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    /** 사용자 ID -> 세션 ID 매핑 (역방향 조회용) */
    private final Map<String, String> userToSession = new ConcurrentHashMap<>();

    /**
     * 사용자 접속 처리
     * @param userId 사용자 ID
     * @param username 사용자 이름
     */
    public void addUser(String userId, String username) {
        onlineUsers.put(userId, username);
    }

    /**
     * 사용자 접속 처리 (세션 정보 포함)
     * @param userId 사용자 ID
     * @param username 사용자 이름
     * @param sessionId 세션 ID
     */
    public void addUser(String userId, String username, String sessionId) {
        onlineUsers.put(userId, username);
        sessionToUser.put(sessionId, userId);
        userToSession.put(userId, sessionId);
    }

    /**
     * 사용자 퇴장 처리
     * @param userId 사용자 ID
     */
    public void removeUser(String userId) {
        onlineUsers.remove(userId);

        // 세션 매핑도 제거
        String sessionId = userToSession.remove(userId);
        if (sessionId != null) {
            sessionToUser.remove(sessionId);
        }
    }

    /**
     * 현재 온라인 사용자 목록 조회
     * @return 온라인 사용자 맵
     */
    public Map<String, String> getOnlineUsers() {
        return new HashMap<>(onlineUsers);
    }

    /**
     * 세션 ID로 사용자 ID 조회
     * @param sessionId 세션 ID
     * @return 사용자 ID (없으면 null)
     */
    public String getUserIdBySessionId(String sessionId) {
        return sessionToUser.get(sessionId);
    }

    /**
     * 사용자 ID로 세션 ID 조회
     * @param userId 사용자 ID
     * @return 세션 ID (없으면 null)
     */
    public String getSessionIdByUserId(String userId) {
        return userToSession.get(userId);
    }

    /**
     * 세션 제거
     * @param sessionId 세션 ID
     */
    public void removeSession(String sessionId) {
        String userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            userToSession.remove(userId);
        }
    }

    /**
     * 메시지 저장
     * 발신자와 수신자 모두의 채팅 히스토리에 저장
     * @param message 저장할 채팅 메시지
     */
    public void saveMessage(ChatMessage message) {
        String sender = message.getSender();
        String receiver = message.getReceiver();

        // 발신자의 채팅 히스토리에 저장
        chatHistory.computeIfAbsent(sender, k -> new ArrayList<>()).add(message);

        // 수신자의 채팅 히스토리에 저장
        if (receiver != null && !receiver.isEmpty()) {
            chatHistory.computeIfAbsent(receiver, k -> new ArrayList<>()).add(message);
        }
    }

    /**
     * 특정 사용자의 전체 채팅 히스토리 조회
     * @param userId 사용자 ID
     * @return 채팅 메시지 리스트
     */
    public List<ChatMessage> getChatHistory(String userId) {
        return chatHistory.getOrDefault(userId, Collections.emptyList());
    }

    /**
     * 두 사용자 간의 1:1 대화 히스토리 조회
     * @param userId1 사용자1 ID
     * @param userId2 사용자2 ID
     * @return 두 사용자 간의 채팅 메시지 리스트
     */
    public List<ChatMessage> getChatHistoryBetween(String userId1, String userId2) {
        List<ChatMessage> messages = chatHistory.getOrDefault(userId1, Collections.emptyList());
        return messages.stream()
                .filter(msg ->
                    (msg.getSender().equals(userId1) && msg.getReceiver().equals(userId2)) ||
                    (msg.getSender().equals(userId2) && msg.getReceiver().equals(userId1))
                )
                .collect(Collectors.toList());
    }
}
