package com.enhorario.repository;

import com.enhorario.model.Establishment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {
    Page<Establishment> findByIsActiveTrue(Pageable pageable);
    
    List<Establishment> findByCategoryIdAndIsActiveTrue(UUID categoryId);
    
    @Query("SELECT e FROM Establishment e WHERE e.isActive = true AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.addressLine) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Establishment> searchEstablishments(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT e FROM Establishment e WHERE e.isActive = true ORDER BY e.averageWaitMinutes ASC")
    List<Establishment> findWithShortestWaitTimes();

    @Modifying
    @Query(value = "UPDATE establishments SET average_wait_minutes = :minutes, updated_at = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int updateAverageWaitMinutes(@Param("id") UUID id, @Param("minutes") int minutes);
}
