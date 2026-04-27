export interface GlobalStatsDto {
    totalNetworks: number;
    totalUsers: number;
    totalCities: number;
    totalCountries: number;
    openNetworkPercent: number;
    wpa3Percent: number;
    band24Percent: number;
    band5Percent: number;
    band6Percent: number;
    avgAccuracy: number;
    categoryDistribution: Record<string, number>;
}
