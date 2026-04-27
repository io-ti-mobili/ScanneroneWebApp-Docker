package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WifiNetworkUploadDto {
    public String bssid;
    public String ssid;
    public Integer frequency;
    public Double realLatitude;
    public Double realLongitude;
    public Float estAccuracy;
    @Builder.Default
    public String category = "OTHER";
    @Builder.Default
    public String security = "OTHER";
    @Builder.Default
    public Float frequencyBand = 0.0f;
}
