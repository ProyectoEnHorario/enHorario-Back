package com.enhorario.service;

import com.enhorario.dto.EstablishmentDTO;
import com.enhorario.model.Establishment;
import com.enhorario.model.FavoriteEstablishment;
import com.enhorario.model.User;
import com.enhorario.repository.EstablishmentRepository;
import com.enhorario.repository.FavoriteEstablishmentRepository;
import com.enhorario.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteEstablishmentService {

    private final FavoriteEstablishmentRepository favoriteEstablishmentRepository;
    private final UserRepository userRepository;
    private final EstablishmentRepository establishmentRepository;

    public List<EstablishmentDTO> getUserFavorites(String userId) {
        UUID userUUID = UUID.fromString(userId);

        return favoriteEstablishmentRepository.findByUserIdOrderByCreatedAtDesc(userUUID)
                .stream()
                .map(FavoriteEstablishment::getEstablishment)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void addFavorite(String userId, String establishmentId) {
        UUID userUUID = UUID.fromString(userId);
        UUID establishmentUUID = UUID.fromString(establishmentId);

        User user = userRepository.findById(userUUID)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Establishment establishment = establishmentRepository.findById(establishmentUUID)
                .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado"));

        boolean alreadyExists = favoriteEstablishmentRepository
                .existsByUserIdAndEstablishmentId(userUUID, establishmentUUID);

        if (alreadyExists) {
            return;
        }

        FavoriteEstablishment favorite = FavoriteEstablishment.builder()
                .user(user)
                .establishment(establishment)
                .build();

        favoriteEstablishmentRepository.save(favorite);
    }

    public void removeFavorite(String userId, String establishmentId) {
        UUID userUUID = UUID.fromString(userId);
        UUID establishmentUUID = UUID.fromString(establishmentId);

        favoriteEstablishmentRepository.deleteByUserIdAndEstablishmentId(userUUID, establishmentUUID);
    }

    private EstablishmentDTO mapToDTO(Establishment establishment) {
        return EstablishmentDTO.builder()
                .id(establishment.getId().toString())
                .name(establishment.getName())
                .ratingAvg(establishment.getRatingAvg() != null ? establishment.getRatingAvg().doubleValue() : null)
                .ratingCount(establishment.getRatingCount())
                .categoryId(establishment.getCategory().getId().toString())
                .categoryName(establishment.getCategory().getName())
                .shortDescription(establishment.getShortDescription())
                .longDescription(establishment.getLongDescription())
                .addressLine(establishment.getAddressLine())
                .city(establishment.getCity())
                .stateRegion(establishment.getStateRegion())
                .country(establishment.getCountry())
                .latitude(establishment.getLatitude() != null ? establishment.getLatitude().doubleValue() : null)
                .longitude(establishment.getLongitude() != null ? establishment.getLongitude().doubleValue() : null)
                .status(establishment.getStatus().toString())
                .openingTime(formatTime(establishment.getOpeningTime()))
                .closingTime(formatTime(establishment.getClosingTime()))
                .websiteUrl(establishment.getWebsiteUrl())
                .phone(establishment.getPhone())
                .whatsappUrl(establishment.getWhatsappUrl())
                .coverPhotoUrl(establishment.getCoverPhotoUrl())
                .logoUrl(establishment.getLogoUrl())
                .averageWaitMinutes(establishment.getAverageWaitMinutes())
                .peakHourStart(formatTime(establishment.getPeakHourStart()))
                .lowHourStart(formatTime(establishment.getLowHourStart()))
                .priceLevel(establishment.getPriceLevel())
                .isVerified(establishment.getIsVerified())
                .isActive(establishment.getIsActive())
                .createdAt(establishment.getCreatedAt() != null ? establishment.getCreatedAt().toString() : null)
                .updatedAt(establishment.getUpdatedAt() != null ? establishment.getUpdatedAt().toString() : null)
                .build();
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.toString() : null;
    }
}
