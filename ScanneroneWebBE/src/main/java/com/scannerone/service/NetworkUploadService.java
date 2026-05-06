package com.scannerone.service;


import com.scannerone.dto.*;
import com.scannerone.entity.User;
import com.scannerone.entity.NetworkUpload;
import com.scannerone.entity.WifiNetwork;
import com.scannerone.repository.UserRepository;
import com.scannerone.repository.NetworkUploadRepository;
import com.scannerone.repository.WifiNetworkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

@Service
public class NetworkUploadService {

    private static final Logger log = LoggerFactory.getLogger(NetworkUploadService.class);
    private static final Pattern BSSID_PATTERN = Pattern.compile("^([0-9A-F]{2}:){5}[0-9A-F]{2}$");

    private final UserService userService;
    private final ScoringService scoringService;
    private final WifiNetworkRepository networkRepository;
    private final NetworkUploadRepository uploadRepository;
    private final UserRepository userRepository;

    private final NominatimProxyService nominatimProxyService;

    public NetworkUploadService(UserService userService, ScoringService scoringService,
                                WifiNetworkRepository networkRepository,
                                NetworkUploadRepository uploadRepository,
                                UserRepository userRepository,
                                NominatimProxyService nominatimProxyService) {
        this.userService = userService;
        this.scoringService = scoringService;
        this.networkRepository = networkRepository;
        this.uploadRepository = uploadRepository;
        this.userRepository = userRepository;
        this.nominatimProxyService = nominatimProxyService;
    }

    /*
     * Entry point principale chiamato dal controller.
     * Per ogni rete nel batch:
     *   1. De-duplicazione per BSSID
     *   2. Calcolo punti
     *   3. Aggiornamento rete se l'upload migliora i dati esistenti
     *   4. Aggiornamento contatori utente
     */
    @Transactional
    public UploadResponseDto processUpload(UploadRequestDto request) {
        int newCount = 0, updatedCount = 0, duplicateCount = 0, totalPoints = 0;
        List<String> newCitiesThisBatch = new ArrayList<>();
        
        if (request.uuid == null || request.uuid.isBlank() || request.password == null || request.password.isBlank()) {
            throw new IllegalArgumentException("UUID e password sono obbligatori");
        }
        
        User user;
        try {
            user = userService.authenticate(request.uuid, request.password);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Credenziali non valide");
        }

        if (request.username != null && !request.username.isBlank()) {
            String sanitized = request.username.trim();
            if (!sanitized.equals(user.getUsername()) && !userRepository.existsByUsername(sanitized)) {
                user.setUsername(sanitized);
            }
        }
        
        List<String> validBssids = new ArrayList<>();
        for (WifiNetworkUploadDto dto : request.networks) {
            String b = dto.bssid != null ? dto.bssid.toUpperCase().trim() : "";
            if (isValidBssid(b)) validBssids.add(b);
        }

        Map<String, WifiNetwork> existingMap = new HashMap<>();
        if (!validBssids.isEmpty()) {
            List<WifiNetwork> existingList = networkRepository.findByBssidIn(validBssids);
            for (WifiNetwork n : existingList) {
                existingMap.put(n.getBssid(), n);
            }
        }

        for (WifiNetworkUploadDto dto : request.networks) {
            String bssid = dto.bssid != null ? dto.bssid.toUpperCase().trim() : "";
            if (!isValidBssid(bssid)) continue;

            WifiNetwork existing = existingMap.get(bssid);
            ScoringService.ScoringResult scoring = scoringService.calculate(dto, existing);

            int points = scoring.points();

            WifiNetwork network;
            boolean requiresGeocoding = false;

            if (existing == null) {
                network = networkRepository.save(toEntity(dto, bssid, user));
                existingMap.put(bssid, network);
                newCount++;
                requiresGeocoding = true;
            } else {
                boolean wasModified = updateNetwork(existing, dto, user);
                if (wasModified) {
                    if (scoring.isAccuracyUpdate() || scoring.isGeoUpdate()) {
                        updatedCount++;
                    } else {
                        duplicateCount++; // technically it changed, but maybe not in accuracy
                    }
                    if (existing.getCity() == null) requiresGeocoding = true; 
                } else {
                    duplicateCount++;
                }
                network = existing;
            }

            if (requiresGeocoding && network.getLatitude() != null && network.getLongitude() != null) {
                network.setNeedsNominatimUpdate(true);
            }

            if (points > 0 || scoring.isFirstDiscovery()) {
                uploadRepository.save(NetworkUpload.builder()
                        .user(user)
                        .network(network)
                        .isFirstDiscovery(scoring.isFirstDiscovery())
                        .isAccuracyUpdate(scoring.isAccuracyUpdate())
                        .isGeoUpdate(scoring.isGeoUpdate())
                        .isNewCityForUser(false) // async city discovery
                        .pointsAwarded(points)
                        .build());
            }

            totalPoints += points;
            
            user.setTotalUploaded(user.getTotalUploaded() + 1);
            if (existing == null) user.setUniqueDiscovered(user.getUniqueDiscovered() + 1);
            user.setScore(user.getScore() + points);
            user.setLastUploadAt(LocalDateTime.now());
        }

        userRepository.save(user);

        // Attiva processo Nominatim asincrono se ci sono stati update
        nominatimProxyService.triggerProcessing();

        return new UploadResponseDto(newCount, updatedCount, duplicateCount,
                totalPoints, user.getScore(), newCitiesThisBatch);
    }

    // --- Helpers privati ---

    private boolean updateNetwork(WifiNetwork existing, WifiNetworkUploadDto dto, User user) {
        boolean modified = false;

        if (dto.realLatitude != null && dto.realLongitude != null && dto.estAccuracy != null) {
            if (existing.getLatitude() == null || existing.getLongitude() == null || existing.getEstAccuracy() == null) {
                existing.setLatitude(dto.realLatitude);
                existing.setLongitude(dto.realLongitude);
                existing.setEstAccuracy(dto.estAccuracy);
                modified = true;
            } else {
                double existingLat = existing.getLatitude();
                double existingLon = existing.getLongitude();
                float existingAcc = existing.getEstAccuracy() != null ? existing.getEstAccuracy() : Float.MAX_VALUE;

                // Simple skip if entirely identical
                if (Math.abs(existingLat - dto.realLatitude) > 0.000001 || 
                    Math.abs(existingLon - dto.realLongitude) > 0.000001 || 
                    Math.abs(existingAcc - dto.estAccuracy) > 0.1) {

                    double eAcc = Math.max(1.0, existing.getEstAccuracy());
                    double nAcc = Math.max(1.0, dto.estAccuracy);
                    double wAccExt = 1.0 / (eAcc * eAcc);
                    double wAccNew = 1.0 / (nAcc * nAcc);
                    double sumAcc = wAccExt + wAccNew;
                    double normAccExt = wAccExt / sumAcc;
                    double normAccNew = wAccNew / sumAcc;

                    long daysOld = java.time.temporal.ChronoUnit.DAYS.between(existing.getLastUpdatedAt(), LocalDateTime.now());
                    if (daysOld < 0) daysOld = 0;
                    double wAgeExt = 1.0 / (1.0 + daysOld);
                    double wAgeNew = 1.0; 
                    double sumAge = wAgeExt + wAgeNew;
                    double normAgeExt = wAgeExt / sumAge;
                    double normAgeNew = wAgeNew / sumAge;

                    double wExt = (2.0 / 3.0) * normAccExt + (1.0 / 3.0) * normAgeExt;
                    double wNew = (2.0 / 3.0) * normAccNew + (1.0 / 3.0) * normAgeNew;
                    double totalW = wExt + wNew;

                    existing.setLatitude((existing.getLatitude() * wExt + dto.realLatitude * wNew) / totalW);
                    existing.setLongitude((existing.getLongitude() * wExt + dto.realLongitude * wNew) / totalW);
                    existing.setEstAccuracy((float) ((existing.getEstAccuracy() * wExt + dto.estAccuracy * wNew) / totalW));
                    modified = true;
                }
            }
        }

        if (modified) {
            existing.setLastUpdatedBy(user);
            existing.setLastUpdatedAt(LocalDateTime.now());
            networkRepository.save(existing);
        }
        return modified;
    }

    private static final Set<String> VALID_CATEGORIES = Set.of("ISP", "FAST_FOOD", "UNIVERSITY", "HOTSPOT", "OTHER");

    private WifiNetwork toEntity(WifiNetworkUploadDto dto, String bssid, User user) {
        String safeCategory = (dto.category != null && VALID_CATEGORIES.contains(dto.category.toUpperCase())) 
                ? dto.category.toUpperCase() : "OTHER";

        return WifiNetwork.builder()
                .bssid(bssid)
                .ssid(dto.ssid)
                .frequency(dto.frequency)
                .latitude(dto.realLatitude)
                .longitude(dto.realLongitude)
                .estAccuracy(dto.estAccuracy)
                .category(safeCategory)
                .security(dto.security)
                .frequencyBand(dto.frequencyBand)
                .firstSeenBy(user)
                .lastUpdatedBy(user)
                .build();
    }

    private boolean isValidBssid(String bssid) {
        if (bssid == null) return false;
        return BSSID_PATTERN.matcher(bssid).matches();
    }
}
