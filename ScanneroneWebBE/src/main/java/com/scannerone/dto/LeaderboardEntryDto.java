package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private long userId;
    private String username;
    private String deviceToken;
    private int score;
    private int uniqueDiscovered;
    private int citiesCovered;
    private Double avgAccuracy;
}
