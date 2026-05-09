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
    public ResponseEntity<TurnDTO> createTurn(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateTurnRequestDTO request) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.createTurn(userId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-turns")
    public ResponseEntity<List<TurnDTO>> getUserTurns(@RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(turnService.getUserTurns(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<List<TurnDTO>> getEstablishmentTurns(@PathVariable String establishmentId) {
        try {
            return ResponseEntity.ok(turnService.getEstablishmentTurns(establishmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{turnId}")
    public ResponseEntity<TurnDTO> getTurnById(@PathVariable String turnId) {
        try {
            return ResponseEntity.ok(turnService.getTurnById(turnId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{turnId}/status")
    public ResponseEntity<TurnDTO> updateTurnStatus(
            @PathVariable String turnId,
            @RequestParam String status) {
        try {
            return ResponseEntity.ok(turnService.updateTurnStatus(turnId, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{turnId}/cancel")
    public ResponseEntity<Void> cancelTurn(
            @PathVariable String turnId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            turnService.cancelTurn(turnId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token inválido");
        }
        return authHeader.replace("Bearer ", "");
    }
}
