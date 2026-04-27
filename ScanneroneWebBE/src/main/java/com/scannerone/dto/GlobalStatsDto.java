package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatsDto {
    public long totalNetworks;
    public long totalUsers;
    public long totalCities;
    public long totalCountries;
    public double openNetworkPercent;
    public double wpa3Percent;
    public double band24Percent;
    public double band5Percent;
    public double band6Percent;
    public Double avgAccuracy;
    public java.util.Map<String, Long> categoryDistribution;
}
