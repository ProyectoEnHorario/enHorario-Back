package com.enhorario.service;

import com.enhorario.dto.UserDTO;
import com.enhorario.model.User;
import com.enhorario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Obtiene todos los usuarios
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca usuarios por nombre, apellido o email
     */
    public List<UserDTO> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(query)
                .stream()
                .map(this::mapUserToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID
     */
    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapUserToDTO(user);
    }

    /**
     * Obtiene un usuario por email
     */
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return mapUserToDTO(user);
    }

    /**
     * Actualiza el rol de un usuario
     * REGLAS:
     * - Solo SUPERADMIN puede cambiar roles
     * - No puedes cambiar el rol de un SUPERADMIN (excepto otro SUPERADMIN)
     */
    public UserDTO updateUserRole(String requesterEmail, UUID targetUserId, String newRole) {
        // Validar que el que solicita sea SUPERADMIN
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Usuario solicitante no encontrado"));

        if (!requester.getRole().equals(User.UserRole.SUPERADMIN)) {
            throw new RuntimeException("No tienes permisos para cambiar roles");
        }

        // Obtener el usuario a modificar
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario objetivo no encontrado"));

        // Validar que no intentes cambiar rol de otro SUPERADMIN
        if (targetUser.getRole().equals(User.UserRole.SUPERADMIN) && 
            !requester.getId().equals(targetUser.getId())) {
            throw new RuntimeException("No puedes cambiar el rol de otro SUPERADMIN");
        }

        // Validar rol válido
        User.UserRole roleToSet;
        try {
            roleToSet = User.UserRole.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido. Roles válidos: SUPERADMIN, ADMIN, ADMIN_LOCAL, USER");
        }

        targetUser.setRole(roleToSet);
        User updatedUser = userRepository.save(targetUser);
        return mapUserToDTO(updatedUser);
    }

    /**
     * Elimina un usuario (soft delete con deletedAt)
     * REGLAS:
     * - Solo SUPERADMIN puede eliminar usuarios
     * - No puedes eliminar otro SUPERADMIN
     */
    public void deleteUser(String requesterEmail, UUID targetUserId) {
        // Validar que el que solicita sea SUPERADMIN
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Usuario solicitante no encontrado"));

        if (!requester.getRole().equals(User.UserRole.SUPERADMIN)) {
            throw new RuntimeException("No tienes permisos para eliminar usuarios");
        }

        // Obtener el usuario a eliminar
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que no intentes eliminar otro SUPERADMIN
        if (targetUser.getRole().equals(User.UserRole.SUPERADMIN) && 
            !requester.getId().equals(targetUser.getId())) {
            throw new RuntimeException("No puedes eliminar a otro SUPERADMIN");
        }

        // Soft delete
        targetUser.setIsActive(false);
        targetUser.setDeletedAt(java.time.OffsetDateTime.now());
        userRepository.save(targetUser);
    }

    /**
     * Mapea User a UserDTO
     */
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
