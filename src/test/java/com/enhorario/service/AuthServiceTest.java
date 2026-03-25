package com.enhorario.service;

import com.enhorario.dto.LoginRequestDTO;
import com.enhorario.dto.LoginResponseDTO;
import com.enhorario.dto.RegisterRequestDTO;
import com.enhorario.model.User;
import com.enhorario.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .name("Test")
                .lastName("User")
                .passwordHash("hashedPassword")
                .role(User.UserRole.USER)
                .isActive(true)
                .build();
    }

    @Test
    void testLoginSuccess() {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password123");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("test@example.com")).thenReturn("token123");

        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertNotNull(response.getUser());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testLoginFailsWithInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "wrongpassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequestDTO request = new RegisterRequestDTO("John", "Doe", "john@example.com", "1234567890", "password123");
        
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> authService.register(request));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterFailsWithDuplicateEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO("John", "Doe", "test@example.com", "1234567890", "password123");
        
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }
}
