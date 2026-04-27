package com.scannerone.controller;

import com.scannerone.dto.ChartDataDto;
import com.scannerone.service.WifiMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class WifiMetricsController {

    private final WifiMetricsService wifiMetricsService;

    /**
     * Restituisce la distribuzione delle reti raggruppate per tipo di sicurezza.
     * Ideale per un Grafico a Torta (Pie Chart) o a Ciambella (Doughnut Chart).
     */
    @GetMapping("/stats/security")
    public ResponseEntity<List<ChartDataDto>> getSecurityDistribution(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getSecurityDistribution(country, region));
    }

    /**
     * Restituisce la distribuzione delle reti raggruppate per categoria.
     * Ideale per un Grafico a Torta (Pie Chart) o a Barre (Bar Chart).
     */
    @GetMapping("/categories")
    public ResponseEntity<List<ChartDataDto>> getCategoryDistribution(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getCategoryDistribution(country, region));
    }

    /**
     * Restituisce i contatori specifici per reti OPEN, WPA3 e le bande di frequenza (2.4, 5, 6 GHz).
     * Ideale per KPI Cards, Contatori Numerici o Widget riassuntivi.
     */
    @GetMapping("/frequencies")
    public ResponseEntity<Map<String, Long>> getQuickStats(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getQuickStats(country, region));
    }

    /**
     * Restituisce il numero di reti scoperte per ogni giorno.
     * Ideale per un Grafico a Linee (Line Chart) o ad Area per mostrare il trend di crescita nel tempo.
     */
    @GetMapping("/stats/daily-discoveries")
    public ResponseEntity<List<ChartDataDto>> getDiscoveryTimeline(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getDiscoveryTimeline(country, region));
    }

    /**
     * Restituisce la precisione media (accuracy) globale della posizione delle reti.
     * Ideale per un indicatore singolo, KPI Card o Gauge Chart.
     */
    @GetMapping("/quality/accuracy")
    public ResponseEntity<Double> getAverageAccuracy(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getAverageAccuracy(country, region));
    }

    /**
     * Restituisce il numero totale di città e nazioni uniche coperte dal progetto.
     * Ideale per KPI Cards riassuntive della copertura globale.
     */
    @GetMapping("/geo/summary")
    public ResponseEntity<Map<String, Long>> getGeoSummary(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(wifiMetricsService.getGeoSummary(country, region));
    }

    /**
     * Restituisce la top-list delle città con il maggior numero di reti scoperte.
     * Ideale per una Classifica (List) o un Grafico a Barre Orizzontali (Horizontal Bar Chart).
     */
    @GetMapping("/geo/top-cities")
    public ResponseEntity<List<ChartDataDto>> getTopCities(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(wifiMetricsService.getTopCities(country, region, limit));
    }

    /**
     * Restituisce la top-list delle città con più reti WPA3.
     */
    @GetMapping("/geo/top-cities/wpa3")
    public ResponseEntity<List<ChartDataDto>> getTopCitiesWpa3(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(wifiMetricsService.getTopCitiesWpa3(country, region, limit));
    }

    /**
     * Restituisce la top-list delle città "meno sicure" (percentuale OPEN più alta).
     */
    @GetMapping("/geo/top-cities/low-security")
    public ResponseEntity<List<ChartDataDto>> getTopCitiesLowSecurity(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(wifiMetricsService.getTopCitiesLowSecurity(country, region, limit));
    }

    /**
     * Restituisce la top-list delle regioni con il maggior numero di reti scoperte.
     * Ideale per una Classifica (List) o un Grafico a Barre Orizzontali (Horizontal Bar Chart).
     */
    @GetMapping("/geo/top-regions")
    public ResponseEntity<List<ChartDataDto>> getTopRegions(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(wifiMetricsService.getTopRegions(country, limit));
    }

    /**
     * Restituisce la top-list delle nazioni con il maggior numero di reti scoperte.
     * Ideale per una Classifica (List) o un Grafico a Barre Orizzontali (Horizontal Bar Chart).
     */
    @GetMapping("/geo/top-countries")
    public ResponseEntity<List<ChartDataDto>> getTopCountries(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(wifiMetricsService.getTopCountries(limit));
    }

    /**
     * Restituisce una lista paginata di tutte le nazioni disponibili.
     * Ideale per popolare menu a tendina (Dropdown) e filtri di ricerca nel frontend.
     */
    @GetMapping("/geo/countries")
    public ResponseEntity<List<String>> getCountries(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(wifiMetricsService.getCountries(limit, offset));
    }

    /**
     * Restituisce il numero di caricamenti (upload) effettuati per ogni giorno.
     */
    @GetMapping("/timeline/uploads")
    public ResponseEntity<List<ChartDataDto>> getUploadsTimeline() {
        return ResponseEntity.ok(wifiMetricsService.getUploadsTimeline());
    }

    /**
     * Restituisce una lista paginata di regioni (eventualmente filtrate per nazione).
     * Ideale per menu a tendina a cascata (Cascading Dropdown).
     */
    @GetMapping("/geo/regions")
    public ResponseEntity<List<String>> getRegions(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(wifiMetricsService.getRegions(country, limit, offset));
    }

    /**
     * Restituisce una lista paginata di città (eventualmente filtrate per nazione e/o regione).
     * Ideale per menu a tendina a cascata (Cascading Dropdown).
     */
    @GetMapping("/geo/cities")
    public ResponseEntity<List<String>> getCities(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(wifiMetricsService.getCities(country, region, limit, offset));
    }
}
