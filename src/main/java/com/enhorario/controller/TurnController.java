package com.enhorario.controller;

import com.enhorario.dto.CreateTurnRequestDTO;
import com.enhorario.dto.TurnDTO;
import com.enhorario.service.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/turns")
@RequiredArgsConstructor
public class TurnController {

    private final TurnService turnService;

    @PostMapping
    public ResponseEntity<Object> createTurn(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateTurnRequestDTO request) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.createTurn(userId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al crear turno");
        }
    }

    @GetMapping("/my-turns")
    public ResponseEntity<Object> getUserTurns(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.getUserTurns(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al obtener turnos");
        }
    }

    // ENH-373: Obtener turnos activos del usuario
    @GetMapping("/my-turns/active")
    public ResponseEntity<Object> getActiveUserTurns(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.getActiveUserTurns(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al obtener turnos activos");
        }
    }

    // ENH-373: Obtener historial de turnos del usuario
    @GetMapping("/my-turns/history")
    public ResponseEntity<Object> getUserTurnsHistory(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.getUserTurnsHistory(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al obtener historial de turnos");
        }
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<Object> getEstablishmentTurns(@PathVariable String establishmentId) {
        try {
            return ResponseEntity.ok(turnService.getEstablishmentTurns(establishmentId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al obtener turnos de establecimiento");
        }
    }

    @GetMapping("/{turnId}")
    public ResponseEntity<Object> getTurnById(@PathVariable String turnId) {
        try {
            return ResponseEntity.ok(turnService.getTurnById(turnId));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage() != null ? e.getMessage() : "Turno no encontrado");
        }
    }

    @PutMapping("/{turnId}/status")
    public ResponseEntity<Object> updateTurnStatus(
            @PathVariable String turnId,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(turnService.updateTurnStatus(turnId, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al actualizar estado");
        }
    }

    @DeleteMapping("/{turnId}/cancel")
    public ResponseEntity<Object> cancelTurn(
            @PathVariable String turnId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            turnService.cancelTurn(turnId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() != null ? e.getMessage() : "Error al cancelar turno");
        }
    }

    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido");
        }
        return authHeader.replace("Bearer ", "");
    }
}
