package com.scannerone.service;

import com.scannerone.dto.ChartDataDto;
import com.scannerone.repository.NetworkUploadRepository;
import com.scannerone.repository.WifiNetworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WifiMetricsService {

    private final WifiNetworkRepository wifiNetworkRepository;
    private final NetworkUploadRepository networkUploadRepository;

    public List<ChartDataDto> getSecurityDistribution(String country, String region) {
        country = sanitize(country);
        region = sanitize(region);
        return wifiNetworkRepository.countBySecurity(country, region).stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getCategoryDistribution(String country, String region) {
        country = sanitize(country);
        region = sanitize(region);
        return wifiNetworkRepository.countNetworksByCategory(country, region).stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getQuickStats(String country, String region) {
        country = sanitize(country);
        region = sanitize(region);
        Map<String, Long> stats = new HashMap<>();
        stats.put("openNetworks", wifiNetworkRepository.countOpenNetworks(country, region));
        stats.put("wpa3Networks", wifiNetworkRepository.countWpa3Networks(country, region));
        stats.put("band24Networks", wifiNetworkRepository.countBand24Networks(country, region));
        stats.put("band5Networks", wifiNetworkRepository.countBand5Networks(country, region));
        stats.put("band6Networks", wifiNetworkRepository.countBand6Networks(country, region));
        return stats;
    }

    public List<ChartDataDto> getDiscoveryTimeline(String country, String region) {
        country = sanitize(country);
        region = sanitize(region);
        List<Object[]> results = wifiNetworkRepository.countNetworksByDate(country, region);
        int skip = Math.max(0, results.size() - 6);
        return results.stream()
                .skip(skip)
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN_DATE",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getTopRegions(String country, int limit) {
        List<Object[]> results;
        if (country != null && !country.isEmpty() && !country.equals("Global")) {
            results = wifiNetworkRepository.findTopRegionsByCountry(country, limit);
        } else {
            results = wifiNetworkRepository.findTopRegionsByNetworkCount(limit);
        }
        return results.stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getTopCountries(int limit) {
        return wifiNetworkRepository.findTopCountriesByNetworkCount(limit).stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getTopCitiesWpa3(String country, String region, int limit) {
        country = sanitize(country);
        region = sanitize(region);
        
        List<Object[]> results;
        if (country == null) {
            results = wifiNetworkRepository.findTopCountriesByWpa3Count(limit);
        } else if (region == null) {
            results = wifiNetworkRepository.findTopRegionsByWpa3Count(country, limit);
        } else {
            results = wifiNetworkRepository.findTopCitiesByWpa3CountFiltered(country, region, limit);
        }
        
        return results.stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getTopCitiesLowSecurity(String country, String region, int limit) {
        country = sanitize(country);
        region = sanitize(region);
        
        List<Object[]> results;
        if (country == null) {
            results = wifiNetworkRepository.findTopCountriesByOpenRatio(limit);
        } else if (region == null) {
            results = wifiNetworkRepository.findTopRegionsByOpenRatio(country, limit);
        } else {
            results = wifiNetworkRepository.findTopCitiesByOpenRatioFiltered(country, region, limit);
        }
        
        return results.stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        Math.round(((Number) obj[1]).doubleValue() * 100) // Percentage 0-100
                ))
                .collect(Collectors.toList());
    }

    public List<ChartDataDto> getTopCities(String country, String region, int limit) {
        country = sanitize(country);
        region = sanitize(region);
        return wifiNetworkRepository.findTopCitiesByNetworkCountFiltered(country, region, limit).stream()
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getGeoSummary(String country, String region) {
        Map<String, Long> summary = new HashMap<>();
        summary.put("distinctCities", wifiNetworkRepository.countDistinctCities(country, region));
        summary.put("distinctCountries", wifiNetworkRepository.countDistinctCountries()); // Global total
        return summary;
    }

    public Double getAverageAccuracy(String country, String region) {
        return wifiNetworkRepository.avgAccuracy(country, region);
    }

    public List<String> getCountries(int limit, int offset) {
        return wifiNetworkRepository.findDistinctCountriesPaginated(limit, offset);
    }

    public List<ChartDataDto> getUploadsTimeline() {
        List<Object[]> results = networkUploadRepository.countGlobalDailyUploads();
        int skip = Math.max(0, results.size() - 6);
        return results.stream()
                .skip(skip)
                .map(obj -> new ChartDataDto(
                        obj[0] != null ? obj[0].toString() : "UNKNOWN_DATE",
                        ((Number) obj[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    public List<String> getRegions(String country, int limit, int offset) {
        return wifiNetworkRepository.findDistinctRegionsPaginated(country, limit, offset);
    }

    public List<String> getCities(String country, String region, int limit, int offset) {
        return wifiNetworkRepository.findDistinctCitiesPaginated(sanitize(country), sanitize(region), limit, offset);
    }

    private String sanitize(String value) {
        if (value == null || value.isEmpty() || "Global".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }
}
