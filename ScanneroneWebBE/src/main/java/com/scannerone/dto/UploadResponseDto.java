package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDto {
    public int newNetworks;
    public int updatedNetworks;
    public int duplicates;
    public int pointsAwarded;
    public int totalScore;
    public List<String> newCitiesDiscovered;
}
