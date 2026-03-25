package com.enhorario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTurnRequestDTO {
    private String establishmentId;
    private String turnType;  // REGULAR or PRIORITY
}
