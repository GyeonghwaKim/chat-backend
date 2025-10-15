package com.example.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 모델
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /** 메시지 고유 ID */
    private String id;

    /** 메시지 내용 */
    private String content;

    /** 발신자 ID */
    private String sender;

    /** 수신자 ID */
    private String receiver;

    /** 메시지 전송 시간 */
    private LocalDateTime timestamp;

    /** 메시지 타입 (JOIN, CHAT, LEAVE) */
    private MessageType type;
}
