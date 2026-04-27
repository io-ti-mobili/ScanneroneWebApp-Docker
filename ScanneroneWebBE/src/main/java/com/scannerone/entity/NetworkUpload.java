package com.scannerone.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/*
 * Registra ogni singola rete inviata da un utente in un batch di upload.
 * Serve per:
 *   - audit trail (chi ha caricato cosa e quando)
 *   - calcolo punti storico
 *   - statistiche per utente (città scoperte, reti uniche, ecc.)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "network_uploads",
        indexes = {
                @Index(name = "idx_upload_user",    columnList = "user_id"),
                @Index(name = "idx_upload_network", columnList = "network_id"),
                @Index(name = "idx_upload_at",      columnList = "uploadedAt")
        }
)
public class NetworkUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", nullable = false)
    private WifiNetwork network;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean isFirstDiscovery = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAccuracyUpdate = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isGeoUpdate = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean isNewCityForUser = false;

    @Builder.Default
    @Column(nullable = false)
    private int pointsAwarded = 0;

}
