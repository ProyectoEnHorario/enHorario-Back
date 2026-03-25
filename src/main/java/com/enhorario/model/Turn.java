package com.enhorario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "turns", indexes = {
    @Index(name = "turns_establishment_status_idx", columnList = "establishment_id, status"),
    @Index(name = "turns_user_status_idx", columnList = "user_id, status"),
    @Index(name = "turns_requested_at_idx", columnList = "requested_at"),
    @Index(name = "turns_turn_code_idx", columnList = "turn_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @Column(name = "turn_code", nullable = false, length = 20)
    private String turnCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "turn_type", nullable = false)
    private TurnType turnType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TurnStatus status;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "attended_at")
    private LocalDateTime attendedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "estimated_attention_at")
    private LocalDateTime estimatedAttentionAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

    public enum TurnType {
        REGULAR, PRIORITY
    }

    public enum TurnStatus {
        WAITING, CALLED, ATTENDED, CANCELLED, EXPIRED
    }
}
