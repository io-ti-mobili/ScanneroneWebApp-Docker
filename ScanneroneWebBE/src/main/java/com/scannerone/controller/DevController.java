package com.scannerone.controller;

import com.scannerone.entity.User;
import com.scannerone.entity.WifiNetwork;
import com.scannerone.repository.UserRepository;
import com.scannerone.repository.WifiNetworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserRepository userRepository;
    private final WifiNetworkRepository wifiNetworkRepository;

    private static final String[] CITIES = {"Rome", "Milan", "Naples", "Turin", "Palermo", "Genoa", "Bologna", "Florence", "Bari", "Catania"};
    private static final String[] REGIONS = {"Lazio", "Lombardy", "Campania", "Piedmont", "Sicily", "Liguria", "Emilia-Romagna", "Tuscany", "Apulia", "Sicily"};
    private static final String[] COUNTRIES = {"Italy"};
    private static final String[] SSIDS = {"Home-WiFi", "Guest-Network", "Starbucks-Free", "Airport-WiFi", "Enterprise-Net", "Linksys", "TP-Link", "D-Link", "Netgear", "ASUS"};
    private static final String[] SECURITIES = {"WPA2-PSK", "WPA3", "OPEN", "WEP", "WPA2-EAP"};
    private static final String[] CATEGORIES = {"HOME", "SHOP", "RESTAURANT", "OFFICE", "TRANSIT", "OTHER"};

    @PostMapping("/seed")
    public ResponseEntity<String> seedData() {
        Random random = new Random();
        List<User> users = new ArrayList<>();

        // 1. Create 10 users
        for (int i = 1; i <= 10; i++) {
            String username = "User" + i;
            String deviceToken = UUID.randomUUID().toString();
            User user = new User(deviceToken, username);
            user.setScore(random.nextInt(5000));
            user.setTotalUploaded(random.nextInt(200));
            user.setUniqueDiscovered(random.nextInt(100));
            user.setCitiesCovered(random.nextInt(5) + 1);
            users.add(userRepository.save(user));
        }

        // 2. Create 100 networks
        for (int i = 0; i < 100; i++) {
            String bssid = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                    random.nextInt(256), random.nextInt(256), random.nextInt(256),
                    random.nextInt(256), random.nextInt(256), random.nextInt(256));
            
            String ssid = SSIDS[random.nextInt(SSIDS.length)] + "-" + random.nextInt(1000);
            int cityIdx = random.nextInt(CITIES.length);
            String city = CITIES[cityIdx];
            String region = REGIONS[cityIdx];
            String country = COUNTRIES[0];
            String security = SECURITIES[random.nextInt(SECURITIES.length)];
            String category = CATEGORIES[random.nextInt(CATEGORIES.length)];
            int frequency = 2400 + random.nextInt(4000);
            float band = frequency < 3000 ? 2.4f : (frequency < 6000 ? 5.0f : 6.0f);
            
            User owner = users.get(random.nextInt(users.size()));
            
            WifiNetwork network = WifiNetwork.builder()
                    .bssid(bssid)
                    .ssid(ssid)
                    .frequency(frequency)
                    .frequencyBand(band)
                    .latitude(41.9 + (random.nextDouble() - 0.5) * 5) // Distributed around Italy
                    .longitude(12.5 + (random.nextDouble() - 0.5) * 5)
                    .estAccuracy(random.nextFloat() * 20 + 1)
                    .city(city)
                    .region(region)
                    .country(country)
                    .security(security)
                    .category(category)
                    .firstSeenBy(owner)
                    .lastUpdatedBy(owner)
                    .firstSeenAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                    .lastUpdatedAt(LocalDateTime.now())
                    .needsNominatimUpdate(false)
                    .build();
            
            wifiNetworkRepository.save(network);
        }

        return ResponseEntity.ok("Successfully seeded 10 users and 100 networks.");
    }
}
