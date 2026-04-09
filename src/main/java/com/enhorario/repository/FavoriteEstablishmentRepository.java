package com.enhorario.repository;

import com.enhorario.model.FavoriteEstablishment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FavoriteEstablishmentRepository extends JpaRepository<FavoriteEstablishment, UUID> {
    List<FavoriteEstablishment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndEstablishmentId(UUID userId, UUID establishmentId);

    void deleteByUserIdAndEstablishmentId(UUID userId, UUID establishmentId);
}
