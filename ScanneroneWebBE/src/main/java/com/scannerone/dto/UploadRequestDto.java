package com.scannerone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadRequestDto {
    @NotBlank(message = "Username non può essere vuoto")
    public String username;
    public String uuid;
    public List<WifiNetworkUploadDto> networks;
}
