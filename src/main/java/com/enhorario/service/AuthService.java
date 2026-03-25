package com.enhorario.service;

import com.enhorario.dto.LoginRequestDTO;
import com.enhorario.dto.LoginResponseDTO;
import com.enhorario.dto.RegisterRequestDTO;
import com.enhorario.dto.UserDTO;
import com.enhorario.model.User;
import com.enhorario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        
        if (user.isEmpty() || !passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        User foundUser = user.get();
        String token = jwtService.generateToken(foundUser.getEmail());
        
        return LoginResponseDTO.builder()
                .token(token)
                .user(mapUserToDTO(foundUser))
                .build();
    }

    public UserDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User newUser = User.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.USER)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);
        return mapUserToDTO(savedUser);
    }

    public UserDTO getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        return mapUserToDTO(user.get());
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .phone(user.getPhone())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }
}
