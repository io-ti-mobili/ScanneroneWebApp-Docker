package com.scannerone.service;


import com.scannerone.dto.WifiNetworkUploadDto;
import com.scannerone.entity.WifiNetwork;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    public static final int POINTS_NEW_NETWORK       = 5;
    public static final int POINTS_ACCURACY_EXCELLENT = 3;  // accuracy < 10m
    public static final int POINTS_ACCURACY_GOOD      = 1;  // accuracy < 30m
    public static final int POINTS_ACCURACY_UPDATE    = 2;  // miglioramento accuracy su rete esistente
    public static final int POINTS_NEW_CITY_FOR_USER  = 10; // prima rete in una città nuova per quell'utente

    public record ScoringResult(
            int points,
            boolean isFirstDiscovery,
            boolean isAccuracyUpdate,
            boolean isGeoUpdate
    ) {}

    /*
     * Calcola i punti per una rete in arrivo rispetto allo stato attuale del DB.
     * existing == null → rete mai vista prima
     * existing != null → rete già presente, valutiamo se il nuovo upload migliora qualcosa
     */
    public ScoringResult calculate(WifiNetworkUploadDto incoming, WifiNetwork existing) {
        if (existing == null) {
            int points = POINTS_NEW_NETWORK
                    + accuracyBonus(incoming.estAccuracy);
            return new ScoringResult(points, true, false, false);
        }

        boolean improvedAccuracy = isAccuracyImprovement(incoming.estAccuracy, existing.getEstAccuracy());

        if (!improvedAccuracy) {
            return new ScoringResult(0, false, false, false);
        }

        int points = POINTS_ACCURACY_UPDATE + accuracyBonus(incoming.estAccuracy);

        return new ScoringResult(points, false, improvedAccuracy, false);
    }

    public int newCityBonus() {
        return POINTS_NEW_CITY_FOR_USER;
    }

    // --- Helpers privati ---

    private int accuracyBonus(Float accuracy) {
        if (accuracy == null) return 0;
        if (accuracy < 10f)   return POINTS_ACCURACY_EXCELLENT;
        if (accuracy < 30f)   return POINTS_ACCURACY_GOOD;
        return 0;
    }

    private boolean isAccuracyImprovement(Float incoming, Float existing) {
        if (incoming == null) return false;
        if (existing == null) return true;       
        return incoming < existing;              
    }
}
