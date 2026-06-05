package com.scannerone.controller;

import com.scannerone.dto.UploadRequestDto;
import com.scannerone.dto.WifiNetworkUploadDto;
import com.scannerone.entity.User;
import com.scannerone.repository.WifiNetworkRepository;
import com.scannerone.service.NetworkUploadService;
import com.scannerone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserService userService;
    private final NetworkUploadService networkUploadService;
    private final WifiNetworkRepository wifiNetworkRepository;

    private static final String[] SSIDS = {"Vodafone-WiFi", "TIM-1234", "Fastweb-Net", "McDonalds-Free", "eduroam", "iPhone di Luca", "AndroidAP", "Home-WiFi", "TP-Link", "ASUS"};
    private static final String[] SECURITIES = {"WPA2-PSK", "WPA3", "OPEN", "WEP", "WPA2-EAP"};
    private static final String[] CATEGORIES = {"ISP", "FAST_FOOD", "UNIVERSITY", "HOTSPOT", "OTHER"};

    @PostMapping("/seed")
    public ResponseEntity<String> seedData() {
        Random random = new Random();
        List<User> users = new ArrayList<>();

        // 1. Create 10 users
        for (int i = 1; i <= 10; i++) {
            User user = userService.registerUser();
            // Let's set a specific username for easier tracking in dev
            userService.updateUsername(user.getId(), "UserDev" + i + "_" + random.nextInt(1000));
            // We need to re-fetch the user to ensure we have the updated username or just use the updated one
            user = userService.findById(user.getId());
            users.add(user);
        }

        // 2. Create 100 networks distributed across users
        Map<User, List<WifiNetworkUploadDto>> userNetworksMap = new HashMap<>();
        for (User user : users) {
            userNetworksMap.put(user, new ArrayList<>());
        }

        for (int i = 0; i < 100; i++) {
            String bssid = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                    random.nextInt(256), random.nextInt(256), random.nextInt(256),
                    random.nextInt(256), random.nextInt(256), random.nextInt(256));
            
            String ssid = SSIDS[random.nextInt(SSIDS.length)] + "-" + random.nextInt(1000);
            String security = SECURITIES[random.nextInt(SECURITIES.length)];
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            int frequency = 2400 + random.nextInt(4000);
            float band = frequency < 3000 ? 2.4f : (frequency < 6000 ? 5.0f : 6.0f);
            
            User owner = users.get(random.nextInt(users.size()));
            
            WifiNetworkUploadDto networkDto = WifiNetworkUploadDto.builder()
                    .bssid(bssid)
                    .ssid(ssid)
                    .frequency(frequency)
                    .frequencyBand(band)
                    .realLatitude(41.9 + (random.nextDouble() - 0.5) * 5) // Distributed around Italy
                    .realLongitude(12.5 + (random.nextDouble() - 0.5) * 5)
                    .estAccuracy(random.nextFloat() * 20 + 1)
                    .security(security)
                    .category(category)
                    .build();
            
            userNetworksMap.get(owner).add(networkDto);
        }

        // 3. Process uploads for each user
        for (User user : users) {
            List<WifiNetworkUploadDto> userNetworks = userNetworksMap.get(user);
            if (!userNetworks.isEmpty()) {
                UploadRequestDto requestDto = UploadRequestDto.builder()
                        .username(user.getUsername())
                        .uuid(user.getDeviceToken())
                        .password(user.getPassword())
                        .networks(userNetworks)
                        .build();
                
                networkUploadService.processUpload(requestDto);
            }
        }

        return ResponseEntity.ok("Successfully seeded 10 users and 100 networks through the upload pipeline.");
    }

    @GetMapping("/diagnostics/nominatim")
    public ResponseEntity<Map<String, Object>> checkNominatimStatus() {
        long failedCount = wifiNetworkRepository.countFailedNominatimNetworks();
        long queuedCount = wifiNetworkRepository.countQueuedNominatimNetworks();
        Map<String, Object> response = new HashMap<>();
        response.put("failed_networks_count", failedCount);
        response.put("queued_networks_count", queuedCount);
        response.put("is_bugged", failedCount > 0);
        response.put("message", failedCount > 0 
            ? "Ci sono reti bloccate con needsNominatimUpdate=false. Esegui la query SQL per sbloccarle." 
            : "Tutto ok, nessuna rete e' bloccata.");
        return ResponseEntity.ok(response);
    }
}
