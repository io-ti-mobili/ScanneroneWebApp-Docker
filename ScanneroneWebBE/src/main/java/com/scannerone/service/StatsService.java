package com.scannerone.service;


import com.scannerone.dto.*;
import com.scannerone.entity.User;
import com.scannerone.repository.UserRepository;
import com.scannerone.repository.WifiNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final WifiNetworkRepository networkRepository;
    private final UserRepository userRepository;

    public StatsService(WifiNetworkRepository networkRepository, UserRepository userRepository) {
        this.networkRepository = networkRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public GlobalStatsDto getGlobalStats(String country, String region) {
        country = sanitize(country);
        region = sanitize(region);
        
        // Se vogliamo il totale filtrato, dovremmo aggiungere un count() con parametri nel repository.
        // Per ora usiamo il totale globale per mantenere la percentuale corretta sul database intero, 
        // o totalNetworks filtrato se preferisci. Facciamo filtrato per coerenza con la dashboard.
        long totalCount;
        if (country == null && region == null) {
            totalCount = networkRepository.count();
        } else {
            // Usiamo i conteggi filtrati
            totalCount = networkRepository.countOpenNetworks(country, region) + 
                         (networkRepository.countBySecurity(country, region).stream()
                            .filter(row -> !"OPEN".equals(row[0]))
                            .mapToLong(row -> ((Number) row[1]).longValue()).sum());
        }

        double total = (double) totalCount;

        Map<String, Long> categoryDistribution = new HashMap<>();
        for (Object[] row : networkRepository.countNetworksByCategory(country, region)) {
            categoryDistribution.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }

        return new GlobalStatsDto(
                totalCount,
                userRepository.count(),
                networkRepository.countDistinctCities(country, region),
                networkRepository.countDistinctCountries(),
                safePercent(networkRepository.countOpenNetworks(country, region), total),
                safePercent(networkRepository.countWpa3Networks(country, region), total),
                safePercent(networkRepository.countBand24Networks(country, region), total),
                safePercent(networkRepository.countBand5Networks(country, region), total),
                safePercent(networkRepository.countBand6Networks(country, region), total),
                networkRepository.avgAccuracy(country, region),
                categoryDistribution
        );
    }

    @Transactional(readOnly = true)
    public UserStatsDto getUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utente '" + username + "' non trovato"));
        
        long userId = user.getId();
        long rank = userRepository.findRankByScore(userId);
        double totalDiscovered = user.getUniqueDiscovered();

        Map<String, Long> securityBreakdown = new HashMap<>();
        for (Object[] row : networkRepository.countBySecurityForUser(userId)) {
            securityBreakdown.put((String) row[0], (Long) row[1]);
        }

        double geoPercent = totalDiscovered > 0
                ? networkRepository.countFullGeoForUser(userId) / totalDiscovered * 100.0
                : 0.0;

        String displayName = user.getUsername() != null
                ? user.getUsername()
                : "Scanner#" + user.getDeviceToken().replace("-", "").substring(0, 6);

        return new UserStatsDto(
                user.getId(), displayName, user.getScore(), (int) rank,
                user.getTotalUploaded(), user.getUniqueDiscovered(),
                user.getCitiesCovered(),
                (int) networkRepository.countDistinctCountriesForUser(userId),
                networkRepository.avgAccuracyForUser(userId),
                geoPercent, securityBreakdown, Map.of()
        );
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(LeaderboardSort sort, int limit, int offset) {
        List<User> users = switch (sort) {
            case SCORE             -> userRepository.findTopByScore(limit, offset);
            case UNIQUE_DISCOVERED -> userRepository.findTopByUniqueDiscovered(limit, offset);
            case CITIES            -> userRepository.findTopByCitiesCovered(limit, offset);
        };

        int[] rankCounter = {offset + 1};
        return users.stream().map(u -> {
            String name = u.getUsername() != null
                    ? u.getUsername()
                    : "Scanner#" + u.getDeviceToken().replace("-", "").substring(0, 6);
            return new LeaderboardEntryDto(
                    rankCounter[0]++, u.getId(), name, u.getDeviceToken(), u.getScore(),
                    u.getUniqueDiscovered(), u.getCitiesCovered(),
                    networkRepository.avgAccuracyForUser(u.getId())
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopCities(int limit) {
        return toNameCountList(networkRepository.findTopCitiesByNetworkCount(limit));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopRegions(int limit) {
        return toNameCountList(networkRepository.findTopRegionsByNetworkCount(limit));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopCountries(int limit) {
        return toNameCountList(networkRepository.findTopCountriesByNetworkCount(limit));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSecurityDistribution() {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : networkRepository.countBySecurity(null, null)) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    // --- New Chart Stats ---

    @Transactional(readOnly = true)
    public List<ChartDataDto> getLineChartData() {
        return networkRepository.countNetworksByDate(null, null).stream()
                .map(row -> new ChartDataDto(String.valueOf(row[0]), (Number) row[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChartDataDto> getPieChartSecurity() {
        return networkRepository.countBySecurity(null, null).stream()
                .map(row -> new ChartDataDto(String.valueOf(row[0]), (Number) row[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChartDataDto> getBarChartTopGlobal(int limit) {
        return networkRepository.findTopCountriesByNetworkCount(limit).stream()
                .map(row -> new ChartDataDto(String.valueOf(row[0]), (Number) row[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChartDataDto> getTopCitiesByCountry(String country, int limit) {
        return networkRepository.findTopCitiesByCountry(country, limit).stream()
                .map(row -> new ChartDataDto(String.valueOf(row[0]), (Number) row[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SimpleStatDto getSimpleStatTotal() {
        long total = networkRepository.count();
        return new SimpleStatDto(total, "Total Networks", "Number of WiFi networks recorded globally");
    }

    @Transactional(readOnly = true)
    public List<String> getCountriesList() {
        return networkRepository.findDistinctCountries();
    }

    // --- Helpers privati ---

    private String sanitize(String value) {
        if (value == null || value.isEmpty() || "Global".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private double safePercent(long numerator, double total) {
        if (total == 0) return 0.0;
        double percent = (numerator / total) * 100.0;
        return Math.round(percent * 100.0) / 100.0; // Round to 2 decimal places
    }

    private List<Map<String, Object>> toNameCountList(List<Object[]> rows) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", row[0]);
            map.put("count", row[1]);
            return map;
        }).collect(Collectors.toList());
    }
}