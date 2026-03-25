package com.enhorario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String role;
    private String phone;
    private String profilePhotoUrl;
    private Boolean isActive;
    private String createdAt;
}
