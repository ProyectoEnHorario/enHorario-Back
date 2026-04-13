package com.enhorario.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDTO {
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^(?!\\s*$).+", message = "El nombre no puede estar vacio")
    private String name;

    @Size(min = 2, max = 80, message = "El apellido debe tener entre 2 y 80 caracteres")
    @Pattern(regexp = "^(?!\\s*$).+", message = "El apellido no puede estar vacio")
    private String lastName;

    @Size(min = 7, max = 30, message = "El telefono debe tener entre 7 y 30 caracteres")
    @Pattern(regexp = "^[+0-9()\\-\\s]+$", message = "El telefono tiene un formato invalido")
    private String phone;

    @Size(max = 500, message = "La URL de foto no puede superar 500 caracteres")
    @Pattern(regexp = "^(https?://).+", message = "La URL de foto debe iniciar con http:// o https://")
    private String profilePhotoUrl;
}