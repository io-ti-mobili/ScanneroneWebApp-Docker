export interface LeaderboardEntryDto {
    rank: number;
    userId: number;
    username: string;
    deviceToken: string;
    score: number;
    uniqueDiscovered: number;
    citiesCovered: number;
    avgAccuracy: number;
}
