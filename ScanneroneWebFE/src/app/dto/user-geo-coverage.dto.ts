export interface CityCoverageDto {
    city: string;
    total: number;
}

export interface RegionCoverageDto {
    region: string;
    total: number;
    cities: CityCoverageDto[];
}

export interface UserGeoCoverageDto {
    country: string;
    total: number;
    regions: RegionCoverageDto[];
}
