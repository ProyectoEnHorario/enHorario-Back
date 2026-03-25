package com.enhorario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstablishmentDTO {
    private String id;
    private String name;
    private Double ratingAvg;
    private Integer ratingCount;
    private String categoryId;
    private String categoryName;
    private String shortDescription;
    private String longDescription;
    private String addressLine;
    private String city;
    private String stateRegion;
    private String country;
    private Double latitude;
    private Double longitude;
    private String status;
    private String openingTime;
    private String closingTime;
    private String websiteUrl;
    private String phone;
    private String whatsappUrl;
    private String coverPhotoUrl;
    private String logoUrl;
    private Integer averageWaitMinutes;
    private String peakHourStart;
    private String lowHourStart;
    private Short priceLevel;
    private Boolean isVerified;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}
