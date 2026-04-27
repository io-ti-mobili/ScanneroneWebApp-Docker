package com.scannerone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStatDto {
    private Number rawNumber;
    private String title;
    private String description;
}
