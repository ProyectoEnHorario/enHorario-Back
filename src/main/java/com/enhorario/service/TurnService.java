package com.enhorario.service;

import com.enhorario.dto.CreateTurnRequestDTO;
import com.enhorario.dto.TurnDTO;
import com.enhorario.model.Establishment;
import com.enhorario.model.Turn;
import com.enhorario.model.User;
import com.enhorario.repository.EstablishmentRepository;
import com.enhorario.repository.TurnRepository;
import com.enhorario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnService {

    private final TurnRepository turnRepository;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;

    public TurnDTO createTurn(String userId, CreateTurnRequestDTO request) {
        UUID userUUID = UUID.fromString(userId);
        UUID establishmentUUID = UUID.fromString(request.getEstablishmentId());

        User user = userRepository.findById(userUUID)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Establishment establishment = establishmentRepository.findById(establishmentUUID)
                .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));

        Turn.TurnType turnType = Turn.TurnType.valueOf(request.getTurnType());
        List<Turn> existingTurns = turnRepository.findByEstablishmentIdAndStatusOrderByQueuePosition(
                establishmentUUID, Turn.TurnStatus.WAITING);

        int totalByType = (int) existingTurns.stream()
                .filter(t -> t.getTurnType() == turnType)
                .count();
        
        String codePrefix = turnType == Turn.TurnType.PRIORITY ? "P" : "A";
        String turnCode = String.format("%s-%03d", codePrefix, totalByType + 1);

        int priorityCount = (int) existingTurns.stream()
                .filter(t -> t.getTurnType() == Turn.TurnType.PRIORITY)
                .count();

        int position = turnType == Turn.TurnType.PRIORITY ? priorityCount + 1 : existingTurns.size() + 1;

        Turn newTurn = Turn.builder()
                .user(user)
                .establishment(establishment)
                .turnCode(turnCode)
                .turnType(turnType)
                .status(Turn.TurnStatus.WAITING)
                .queuePosition(position)
                .build();

        Turn savedTurn = turnRepository.save(newTurn);
        return mapToDTO(savedTurn);
    }

    public List<TurnDTO> getUserTurns(String userId) {
        UUID userUUID = UUID.fromString(userId);
        return turnRepository.findByUserIdOrderByRequestedAtDesc(userUUID)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TurnDTO> getEstablishmentTurns(String establishmentId) {
        UUID establishmentUUID = UUID.fromString(establishmentId);
        return turnRepository.findByEstablishmentIdAndStatusOrderByQueuePosition(
                establishmentUUID, Turn.TurnStatus.WAITING)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TurnDTO getTurnById(String turnId) {
        UUID turnUUID = UUID.fromString(turnId);
        Optional<Turn> turn = turnRepository.findById(turnUUID);
        if (turn.isEmpty()) {
            throw new RuntimeException("Turno no encontrado");
        }
        return mapToDTO(turn.get());
    }

    public TurnDTO updateTurnStatus(String turnId, String newStatus) {
        UUID turnUUID = UUID.fromString(turnId);
        Turn turn = turnRepository.findById(turnUUID)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        Turn.TurnStatus status = Turn.TurnStatus.valueOf(newStatus);
        turn.setStatus(status);

        if (status == Turn.TurnStatus.CALLED) {
            turn.setCalledAt(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        } else if (status == Turn.TurnStatus.ATTENDED) {
            turn.setAttendedAt(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        } else if (status == Turn.TurnStatus.CANCELLED) {
            turn.setCancelledAt(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        }

        Turn updatedTurn = turnRepository.save(turn);
        recalculatePositions(turn.getEstablishment().getId());
        return mapToDTO(updatedTurn);
    }

    public void cancelTurn(String turnId, String userId) {
        UUID turnUUID = UUID.fromString(turnId);
        UUID userUUID = UUID.fromString(userId);

        Turn turn = turnRepository.findById(turnUUID)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        if (!turn.getUser().getId().equals(userUUID)) {
            throw new RuntimeException("No tienes permisos para cancelar este turno");
        }

        turn.setStatus(Turn.TurnStatus.CANCELLED);
        turn.setCancelledAt(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
        turnRepository.save(turn);

        recalculatePositions(turn.getEstablishment().getId());
    }

    private void recalculatePositions(UUID establishmentId) {
        List<Turn> waitingTurns = turnRepository.findByEstablishmentIdAndStatusOrderByQueuePosition(
                establishmentId, Turn.TurnStatus.WAITING);

        waitingTurns.sort((a, b) -> {
            int prioA = a.getTurnType() == Turn.TurnType.PRIORITY ? 0 : 1;
            int prioB = b.getTurnType() == Turn.TurnType.PRIORITY ? 0 : 1;
            int byType = Integer.compare(prioA, prioB);
            return byType != 0 ? byType : a.getRequestedAt().compareTo(b.getRequestedAt());
        });

        for (int i = 0; i < waitingTurns.size(); i++) {
            waitingTurns.get(i).setQueuePosition(i + 1);
            turnRepository.save(waitingTurns.get(i));
        }
    }

    private TurnDTO mapToDTO(Turn turn) {
        return TurnDTO.builder()
                .id(turn.getId().toString())
                .userId(turn.getUser().getId().toString())
                .establishmentId(turn.getEstablishment().getId().toString())
                .establishmentName(turn.getEstablishment().getName())
                .turnCode(turn.getTurnCode())
                .turnType(turn.getTurnType().toString())
                .status(turn.getStatus().toString())
                .queuePosition(turn.getQueuePosition())
                .requestedAt(turn.getRequestedAt().toString())
                .calledAt(turn.getCalledAt() != null ? turn.getCalledAt().toString() : null)
                .attendedAt(turn.getAttendedAt() != null ? turn.getAttendedAt().toString() : null)
                .cancelledAt(turn.getCancelledAt() != null ? turn.getCancelledAt().toString() : null)
                .estimatedAttentionAt(turn.getEstimatedAttentionAt() != null ? turn.getEstimatedAttentionAt().toString() : null)
                .build();
    }
}
