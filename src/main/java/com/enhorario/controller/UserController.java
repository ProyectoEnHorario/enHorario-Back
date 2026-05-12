package com.enhorario.controller;

import com.enhorario.dto.UpdateRoleRequestDTO;
import com.enhorario.dto.UserDTO;
import com.enhorario.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/v1/users - Obtener todos los usuarios
     * GET /api/v1/users?q=término - Buscar usuarios por nombre, apellido o email
     */
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(value = "q", required = false) String searchQuery,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Authorization header inválido"));
            }

            List<UserDTO> users;
            if (searchQuery != null && !searchQuery.isBlank()) {
                users = userService.searchUsers(searchQuery);
            } else {
                users = userService.getAllUsers();
            }

            // Retornar directamente el array como propone el front
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/v1/users/{uid}/role - Cambiar el rol de un usuario
     */
    @PatchMapping("/{uid}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String uid,
            @Valid @RequestBody UpdateRoleRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Authorization header inválido"));
            }

            String requesterEmail = authHeader.replace("Bearer ", "");
            UUID userId = UUID.fromString(uid);

            UserDTO updatedUser = userService.updateUserRole(requesterEmail, userId, request.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rol actualizado con éxito");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "UUID inválido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * DELETE /api/v1/users/{uid} - Eliminar un usuario
     */
    @DeleteMapping("/{uid}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String uid,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Authorization header inválido"));
            }

            String requesterEmail = authHeader.replace("Bearer ", "");
            UUID userId = UUID.fromString(uid);

            userService.deleteUser(requesterEmail, userId);

            return ResponseEntity.ok(Map.of("message", "Usuario eliminado con éxito"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "UUID inválido"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }
}
