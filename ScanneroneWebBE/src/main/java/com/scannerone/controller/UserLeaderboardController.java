package com.scannerone.controller;

import com.scannerone.dto.LeaderboardEntryDto;
import com.scannerone.service.UserLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserLeaderboardController {

    private final UserLeaderboardService userLeaderboardService;

    /**
     * Restituisce la classifica globale basata sul punteggio totale (Score).
     * Ideale per la vista principale della Tabella Classifiche (Data Table).
     */
    @GetMapping("/api/leaderboard/global")
    public ResponseEntity<List<LeaderboardEntryDto>> getGlobalLeaderboard(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(userLeaderboardService.getGlobalLeaderboard(country, region, city, limit, offset));
    }

    /**
     * Restituisce la classifica basata su chi ha scoperto per primo reti uniche ("First seen").
     * Ideale per un tab secondario della Tabella Classifiche (Data Table) o una classifica "Scopritori".
     */
    @GetMapping("/api/leaderboard/discovery")
    public ResponseEntity<List<LeaderboardEntryDto>> getDiscoveryLeaderboard(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(userLeaderboardService.getDiscoveryLeaderboard(country, region, city, limit, offset));
    }

    /**
     * Restituisce la classifica dei "viaggiatori", ovvero chi ha coperto più città distinte.
     * Ideale per un tab della Tabella Classifiche (Data Table) dedicato agli esploratori.
     */
    @GetMapping("/api/leaderboard/travelers")
    public ResponseEntity<List<LeaderboardEntryDto>> getTravelerLeaderboard(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(userLeaderboardService.getTravelerLeaderboard(limit, offset));
    }

    /**
     * Restituisce la posizione esatta in classifica globale (Rank) per uno specifico utente.
     * Ideale per essere mostrato nella schermata Profilo Utente (es. "Sei in 14° posizione").
     */
    @GetMapping("/api/users/{userId}/rank")
    public ResponseEntity<Long> getUserRank(@PathVariable Long userId) {
        return ResponseEntity.ok(userLeaderboardService.getUserRank(userId));
    }
}
