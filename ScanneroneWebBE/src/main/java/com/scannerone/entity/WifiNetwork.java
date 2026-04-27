package com.scannerone.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "wifi_networks",
        indexes = {
                @Index(name = "idx_bssid",    columnList = "bssid",              unique = true),
                @Index(name = "idx_city",     columnList = "city"),
                @Index(name = "idx_region",   columnList = "region"),
                @Index(name = "idx_country",  columnList = "country"),
                @Index(name = "idx_security", columnList = "security"),
                @Index(name = "idx_latlon",   columnList = "latitude,longitude")
        }
)
public class WifiNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 17)
    private String bssid;

    @Column(nullable = false)
    private String ssid;

    private Integer frequency;

    private Double latitude;
    private Double longitude;
    private Float estAccuracy;
    private String street;
    private String city;
    private String region;
    private String country;
    
    @Builder.Default
    @Column(length = 32)
    private String category = "OTHER";
    
    @Builder.Default
    @Column(length = 16)
    private String security = "OTHER";
    
    @Builder.Default
    private Float frequencyBand = 0.0f;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_seen_by_id")
    private User firstSeenBy;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime firstSeenAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_updated_by_id")
    private User lastUpdatedBy;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean needsNominatimUpdate = true;
}