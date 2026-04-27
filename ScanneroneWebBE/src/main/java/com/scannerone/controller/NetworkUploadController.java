package com.scannerone.controller;

import com.scannerone.dto.UploadRequestDto;
import com.scannerone.dto.UploadResponseDto;
import com.scannerone.dto.WifiNetworkUploadDto;
import com.scannerone.service.NetworkUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class NetworkUploadController {

    private final NetworkUploadService uploadService;

    public NetworkUploadController(NetworkUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/batch")
    public ResponseEntity<UploadResponseDto> uploadNetworks(@RequestBody UploadRequestDto request) {
        if (request == null || request.networks == null || request.networks.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.username == null || request.username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UploadResponseDto result = uploadService.processUpload(request);
        return ResponseEntity.ok(result);
    }
}
