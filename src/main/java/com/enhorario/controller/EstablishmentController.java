package com.enhorario.controller;

import com.enhorario.dto.EstablishmentDTO;
import com.enhorario.dto.RateWaitTimeRequestDTO;
import com.enhorario.dto.ReportWaitTimeRequestDTO;
import com.enhorario.service.EstablishmentService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/establishments")
@RequiredArgsConstructor
public class EstablishmentController {

    private static final Logger log = LoggerFactory.getLogger(EstablishmentController.class);

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

    @PostMapping("/{id}/wait-time")
    public ResponseEntity<?> reportWaitTime(
            @PathVariable String id,
            @RequestBody @Valid ReportWaitTimeRequestDTO request) {
        try {
            establishmentService.reportWaitTime(java.util.UUID.fromString(id), request.getMinutes());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error inesperado al reportar wait-time para establecimiento {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", e.getClass().getSimpleName(),
                            "message", e.getMessage() != null ? e.getMessage() : "Error inesperado"
                    ));
        }
    }

    @PostMapping("/{id}/wait-time/rating")
    public ResponseEntity<?> rateWaitTime(
            @PathVariable String id,
            @RequestBody @Valid RateWaitTimeRequestDTO request) {
        try {
            establishmentService.rateWaitTime(java.util.UUID.fromString(id), request.getRating());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error inesperado al calificar wait-time para establecimiento {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", e.getClass().getSimpleName(),
                            "message", e.getMessage() != null ? e.getMessage() : "Error inesperado"
                    ));
        }
    }
}
