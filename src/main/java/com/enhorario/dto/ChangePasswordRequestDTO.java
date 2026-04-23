package com.enhorario.dto;

import jakarta.validation.constraints.Pattern;
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
public class ChangePasswordRequestDTO {
    @NotBlank(message = "La contrasena actual es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena actual debe tener entre 8 y 100 caracteres")
    private String currentPassword;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La nueva contrasena debe tener entre 8 y 100 caracteres")
    private String newPassword;

    @NotBlank(message = "La confirmacion de contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La confirmacion de contrasena debe tener entre 8 y 100 caracteres")
    private String confirmNewPassword;
}