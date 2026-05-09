package com.enhorario.repository;

import com.enhorario.model.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurnRepository extends JpaRepository<Turn, UUID> {
    List<Turn> findByUserIdOrderByRequestedAtDesc(UUID userId);
    
    List<Turn> findByEstablishmentIdAndStatusOrderByQueuePosition(UUID establishmentId, Turn.TurnStatus status);
    
    Optional<Turn> findByTurnCode(String turnCode);
    
    @Query("SELECT COUNT(t) FROM Turn t WHERE t.establishment.id = :establishmentId " +
           "AND t.status = 'WAITING' AND t.turnType = 'PRIORITY'")
    Integer countPriorityWaitingTurns(@Param("establishmentId") UUID establishmentId);
    
    @Query("SELECT COUNT(t) FROM Turn t WHERE t.establishment.id = :establishmentId " +
           "AND t.status = 'WAITING' AND t.turnType = 'REGULAR'")
    Integer countRegularWaitingTurns(@Param("establishmentId") UUID establishmentId);

    // ENH-373: Métodos para filtrar turnos activos e historial por usuario
    @Query("SELECT t FROM Turn t WHERE t.user.id = :userId AND t.status = 'WAITING' " +
           "ORDER BY t.requestedAt DESC")
    List<Turn> findActiveUserTurns(@Param("userId") UUID userId);

    @Query("SELECT t FROM Turn t WHERE t.user.id = :userId AND t.status IN ('ATTENDED', 'CANCELLED') " +
           "ORDER BY t.requestedAt DESC")
    List<Turn> findUserTurnsHistory(@Param("userId") UUID userId);
}
