package com.enhorario.controller;

import com.enhorario.dto.EstablishmentDTO;
import com.enhorario.service.FavoriteEstablishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteEstablishmentController {

    private final FavoriteEstablishmentService favoriteEstablishmentService;

    @GetMapping("/my-favorites")
    public ResponseEntity<List<EstablishmentDTO>> getMyFavorites(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(favoriteEstablishmentService.getUserFavorites(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{establishmentId}")
    public ResponseEntity<Void> addFavorite(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String establishmentId) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            favoriteEstablishmentService.addFavorite(userId, establishmentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{establishmentId}")
    public ResponseEntity<Void> removeFavorite(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String establishmentId) {
        try {
            String userId = extractUserIdFromToken(authHeader);
            favoriteEstablishmentService.removeFavorite(userId, establishmentId);
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
