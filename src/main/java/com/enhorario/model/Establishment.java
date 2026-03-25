package com.enhorario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "establishments", indexes = {
    @Index(name = "establishments_category_idx", columnList = "category_id"),
    @Index(name = "establishments_status_idx", columnList = "status"),
    @Index(name = "establishments_wait_minutes_idx", columnList = "average_wait_minutes")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Establishment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(name = "rating_avg")
    private Double ratingAvg;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "short_description", length = 240)
    private String shortDescription;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Column(name = "address_line", nullable = false, length = 220)
    private String addressLine;

    @Column(length = 80)
    private String city;

    @Column(name = "state_region", length = 80)
    private String stateRegion;

    @Column(length = 80)
    private String country;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstablishmentStatus status;

    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(length = 30)
    private String phone;

    @Column(name = "whatsapp_url", columnDefinition = "TEXT")
    private String whatsappUrl;

    @Column(name = "cover_photo_url", columnDefinition = "TEXT")
    private String coverPhotoUrl;

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    @Column(name = "average_wait_minutes")
    private Integer averageWaitMinutes;

    @Column(name = "peak_hour_start")
    private String peakHourStart;

    @Column(name = "low_hour_start")
    private String lowHourStart;

    @Column(name = "price_level")
    private Integer priceLevel;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        isActive = true;
        isVerified = false;
        ratingAvg = 0.0;
        ratingCount = 0;
        averageWaitMinutes = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EstablishmentStatus {
        OPEN, CLOSED, PAUSED
    }
}
