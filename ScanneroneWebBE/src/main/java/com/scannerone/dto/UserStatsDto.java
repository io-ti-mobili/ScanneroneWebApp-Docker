package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    public long userId;
    public String username;
    public int score;
    public int rank;
    public int totalUploaded;
    public int uniqueDiscovered;
    public int citiesCovered;
    public int countriesCovered;
    public Double avgAccuracy;
    public double geoCompletionPercent;
    public Map<String, Long> securityBreakdown;
    public Map<String, Long> bandBreakdown;
}
