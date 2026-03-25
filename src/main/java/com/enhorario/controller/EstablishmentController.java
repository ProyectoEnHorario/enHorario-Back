package com.enhorario.controller;

import com.enhorario.dto.EstablishmentDTO;
import com.enhorario.service.EstablishmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private final EstablishmentService establishmentService;

    @GetMapping
    public ResponseEntity<Page<EstablishmentDTO>> getAllEstablishments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(establishmentService.getAllEstablishments(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstablishmentDTO> getEstablishmentById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(establishmentService.getEstablishmentById(java.util.UUID.fromString(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<EstablishmentDTO>> getEstablishmentsByCategory(@PathVariable String categoryId) {
        try {
            return ResponseEntity.ok(establishmentService.getEstablishmentsByCategory(java.util.UUID.fromString(categoryId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EstablishmentDTO>> searchEstablishments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(establishmentService.searchEstablishments(query, page, size));
    }

    @GetMapping("/trending/shortest-wait")
    public ResponseEntity<List<EstablishmentDTO>> getTrendingEstablishments() {
        return ResponseEntity.ok(establishmentService.getEstablishmentsWithShortestWaitTimes());
    }
}
