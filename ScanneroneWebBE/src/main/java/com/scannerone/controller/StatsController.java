package com.scannerone.controller;

import com.scannerone.dto.*;
import com.scannerone.service.LeaderboardSort;
import com.scannerone.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/global")
    public ResponseEntity<GlobalStatsDto> getGlobalStats(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(statsService.getGlobalStats(country, region));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable String username) {
        return ResponseEntity.ok(statsService.getUserStats(username));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(
            @RequestParam(defaultValue = "SCORE") LeaderboardSort sort,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(statsService.getLeaderboard(sort, limit, offset));
    }

    @GetMapping("/top/cities")
    public ResponseEntity<List<Map<String, Object>>> getTopCities(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getTopCities(limit));
    }

    @GetMapping("/top/regions")
    public ResponseEntity<List<Map<String, Object>>> getTopRegions(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getTopRegions(limit));
    }

    @GetMapping("/top/countries")
    public ResponseEntity<List<Map<String, Object>>> getTopCountries(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getTopCountries(limit));
    }

    @GetMapping("/security-distribution")
    public ResponseEntity<Map<String, Long>> getSecurityDistribution() {
        return ResponseEntity.ok(statsService.getSecurityDistribution());
    }

    // --- New Chart APIs ---

    @GetMapping("/line")
    public ResponseEntity<List<ChartDataDto>> getLineChart() {
        return ResponseEntity.ok(statsService.getLineChartData());
    }

    @GetMapping("/pie")
    public ResponseEntity<List<ChartDataDto>> getPieChart() {
        return ResponseEntity.ok(statsService.getPieChartSecurity());
    }

    @GetMapping("/bar")
    public ResponseEntity<List<ChartDataDto>> getBarChart(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getBarChartTopGlobal(limit));
    }

    @GetMapping("/top-cities")
    public ResponseEntity<List<ChartDataDto>> getTopCitiesByCountry(
            @RequestParam String country, 
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getTopCitiesByCountry(country, limit));
    }

    @GetMapping("/simple")
    public ResponseEntity<SimpleStatDto> getSimpleStat() {
        return ResponseEntity.ok(statsService.getSimpleStatTotal());
    }

    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(statsService.getCountriesList());
    }
}
