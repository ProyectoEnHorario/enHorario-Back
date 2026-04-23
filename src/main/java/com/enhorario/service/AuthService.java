package com.enhorario.service;

import com.enhorario.dto.LoginRequestDTO;
import com.enhorario.dto.LoginResponseDTO;
import com.enhorario.dto.MessageResponseDTO;
import com.enhorario.dto.ChangePasswordRequestDTO;
import com.enhorario.dto.ForgotPasswordRequestDTO;
import com.enhorario.dto.ForgotPasswordResponseDTO;
import com.enhorario.dto.RegisterRequestDTO;
import com.enhorario.dto.ResetPasswordRequestDTO;
import com.enhorario.dto.UpdateProfileMultipartRequestDTO;
import com.enhorario.dto.UpdateProfileRequestDTO;
import com.enhorario.dto.UserDTO;
import com.enhorario.model.User;
import com.enhorario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ProfileImageStorageService profileImageStorageService;

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

    public UserDTO updateCurrentUser(String email, UpdateProfileRequestDTO request) {
        return updateCurrentUser(email, request, null);
    }

    public UserDTO updateCurrentUser(String email, UpdateProfileRequestDTO request, MultipartFile profilePhoto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getProfilePhotoUrl() != null && !request.getProfilePhotoUrl().trim().isEmpty()) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl().trim());
        }

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            String storedPhotoUrl = profileImageStorageService.storeProfileImage(profilePhoto);
            user.setProfilePhotoUrl(storedPhotoUrl);
        }

        User updatedUser = userRepository.save(user);
        return mapUserToDTO(updatedUser);
    }

    public UserDTO updateCurrentUser(String email, UpdateProfileMultipartRequestDTO request) {
        UpdateProfileRequestDTO baseRequest = UpdateProfileRequestDTO.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .build();
        return updateCurrentUser(email, baseRequest, request.getProfilePhoto());
    }

    public MessageResponseDTO changePassword(String email, ChangePasswordRequestDTO request) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            throw new RuntimeException("La nueva contrasena y su confirmacion son obligatorias");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Las contrasenas nuevas no coinciden");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("La contrasena actual es incorrecta");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setPasswordResetTokenUsedAt(null);
        userRepository.save(user);

        return MessageResponseDTO.builder()
                .message("Contrasena actualizada correctamente")
                .build();
    }

    public ForgotPasswordResponseDTO requestPasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String resetToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        OffsetDateTime expiresAt = OffsetDateTime.now().plus(PASSWORD_RESET_TOKEN_TTL);

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(expiresAt);
        user.setPasswordResetTokenUsedAt(null);
        userRepository.save(user);

        return ForgotPasswordResponseDTO.builder()
                .message("Se genero un token de reseteo de contrasena")
                .resetToken(resetToken)
                .expiresAt(expiresAt.toString())
                .build();
    }

    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            throw new RuntimeException("La nueva contrasena y su confirmacion son obligatorias");
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Las contrasenas nuevas no coinciden");
        }

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalido o expirado"));

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getPasswordResetTokenExpiresAt() == null || user.getPasswordResetTokenExpiresAt().isBefore(now)) {
            throw new RuntimeException("Token invalido o expirado");
        }
        if (user.getPasswordResetTokenUsedAt() != null) {
            throw new RuntimeException("Token ya utilizado");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
        user.setPasswordResetTokenUsedAt(now);
        userRepository.save(user);

        return MessageResponseDTO.builder()
                .message("Contrasena restablecida correctamente")
                .build();
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
