package com.enhorario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequestDTO {
    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contrasena debe tener entre 8 y 100 caracteres")
    private String newPassword;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La confirmacion de contrasena debe tener entre 8 y 100 caracteres")
    private String confirmNewPassword;
}