package com.enhorario.service;

import com.enhorario.dto.EstablishmentDTO;
import com.enhorario.model.Establishment;
import com.enhorario.repository.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;

    public Page<EstablishmentDTO> getAllEstablishments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return establishmentRepository.findByIsActiveTrue(pageable)
                .map(this::mapToDTO);
    }

    public EstablishmentDTO getEstablishmentById(UUID id) {
        Optional<Establishment> establishment = establishmentRepository.findById(id);
        if (establishment.isEmpty()) {
            throw new RuntimeException("Establecimiento no encontrado");
        }
        return mapToDTO(establishment.get());
    }

    public List<EstablishmentDTO> getEstablishmentsByCategory(UUID categoryId) {
        return establishmentRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<EstablishmentDTO> searchEstablishments(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return establishmentRepository.searchEstablishments(query, pageable)
                .map(this::mapToDTO);
    }

    public List<EstablishmentDTO> getEstablishmentsWithShortestWaitTimes() {
        return establishmentRepository.findWithShortestWaitTimes()
                .stream()
                .limit(10)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EstablishmentDTO mapToDTO(Establishment establishment) {
        return EstablishmentDTO.builder()
                .id(establishment.getId().toString())
                .name(establishment.getName())
                .ratingAvg(establishment.getRatingAvg())
                .ratingCount(establishment.getRatingCount())
                .categoryId(establishment.getCategory().getId().toString())
                .categoryName(establishment.getCategory().getName())
                .shortDescription(establishment.getShortDescription())
                .longDescription(establishment.getLongDescription())
                .addressLine(establishment.getAddressLine())
                .city(establishment.getCity())
                .stateRegion(establishment.getStateRegion())
                .country(establishment.getCountry())
                .latitude(establishment.getLatitude())
                .longitude(establishment.getLongitude())
                .status(establishment.getStatus().toString())
                .openingTime(establishment.getOpeningTime())
                .closingTime(establishment.getClosingTime())
                .websiteUrl(establishment.getWebsiteUrl())
                .phone(establishment.getPhone())
                .whatsappUrl(establishment.getWhatsappUrl())
                .coverPhotoUrl(establishment.getCoverPhotoUrl())
                .logoUrl(establishment.getLogoUrl())
                .averageWaitMinutes(establishment.getAverageWaitMinutes())
                .peakHourStart(establishment.getPeakHourStart())
                .lowHourStart(establishment.getLowHourStart())
                .priceLevel(establishment.getPriceLevel())
                .isVerified(establishment.getIsVerified())
                .isActive(establishment.getIsActive())
                .createdAt(establishment.getCreatedAt().toString())
                .updatedAt(establishment.getUpdatedAt().toString())
                .build();
    }
}
