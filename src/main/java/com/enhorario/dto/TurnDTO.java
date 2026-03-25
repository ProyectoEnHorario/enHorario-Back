package com.enhorario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnDTO {
    private String id;
    private String userId;
    private String establishmentId;
    private String establishmentName;
    private String turnCode;
    private String turnType;
    private String status;
    private Integer queuePosition;
    private String requestedAt;
    private String calledAt;
    private String attendedAt;
    private String cancelledAt;
    private String estimatedAttentionAt;
}
