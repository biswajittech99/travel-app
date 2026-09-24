package com.example.chatbotservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_conversations")
@Getter
@Setter

public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "mode")
    private String mode;       // ✅ tracks which impl was used

    @Column(columnDefinition = "TEXT")
    private String messagesJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Conversation(String id, String userId, String mode, String messagesJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.mode = mode;
        this.messagesJson = messagesJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Conversation() {

    }
}
