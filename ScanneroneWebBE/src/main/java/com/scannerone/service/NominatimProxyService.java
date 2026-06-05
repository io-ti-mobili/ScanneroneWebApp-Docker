package com.scannerone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scannerone.entity.User;
import com.scannerone.entity.WifiNetwork;
import com.scannerone.repository.NetworkUploadRepository;
import com.scannerone.repository.UserRepository;
import com.scannerone.repository.WifiNetworkRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NominatimProxyService {

    private static final Logger log = LoggerFactory.getLogger(NominatimProxyService.class);

    private final WifiNetworkRepository networkRepository;
    private final UserRepository userRepository;
    private final NetworkUploadRepository uploadRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Store coordinate (rounded to 3 decimal places) to Nominatim Response
    private final ConcurrentHashMap<String, GeoResult> geoCache = new ConcurrentHashMap<>();
    
    // Single thread executor ensures only one batch processing at a time
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public NominatimProxyService(WifiNetworkRepository networkRepository, UserRepository userRepository, NetworkUploadRepository uploadRepository) {
        this.networkRepository = networkRepository;
        this.userRepository = userRepository;
        this.uploadRepository = uploadRepository;
    }

    @PostConstruct
    public void start() {
        triggerProcessing(); // Check on app start
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
    }

    public void triggerProcessing() {
        if (isProcessing.compareAndSet(false, true)) {
            executor.submit(() -> {
                try {
                    processBatchLoop();
                } finally {
                    isProcessing.set(false);
                }
            });
        }
    }

    private void processBatchLoop() {
        while (true) {
            try {
                List<WifiNetwork> batch = networkRepository.findTop50ByNeedsNominatimUpdateTrue();
                if (batch.isEmpty()) {
                    break; // No more networks need update, exit loop
                }

                for (WifiNetwork network : batch) {
                    if (network.getLatitude() != null && network.getLongitude() != null) {
                        try {
                            geocodeAndUpdate(network);
                            // Rate limit of 1 second per request to Nominatim
                            Thread.sleep(1100); 
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return; // Stop if interrupted
                        } catch (Exception e) {
                            log.error("Error geocoding network {}: {}", network.getId(), e.getMessage());
                            // L'API ha fallito. Non scartiamo la rete, ma interrompiamo
                            // il ciclo di batch per non spammare l'API offline.
                            throw new RuntimeException("Interrompo il batch per errore Nominatim", e);
                        }
                    } else {
                        network.setNeedsNominatimUpdate(false);
                        networkRepository.save(network);
                    }
                }
            } catch (Exception e) {
                log.error("Error in Nominatim batch loop", e);
                break; // Break on DB errors or other fatal issues
            }
        }
    }

    private void geocodeAndUpdate(WifiNetwork network) {
        String cacheKey = String.format(java.util.Locale.US, "%.3f,%.3f", network.getLatitude(), network.getLongitude());
        GeoResult result = geoCache.get(cacheKey);

        if (result == null) {
            result = fetchFromNominatim(network.getLatitude(), network.getLongitude());
            if (result != null) {
                geoCache.put(cacheKey, result);
            }
        }

        if (result == null) {
            throw new RuntimeException("Nominatim API returned null (blocco o errore)");
        }

        boolean cityChanged = false;
        if (result != null) {
            if (network.getStreet() == null && result.street != null) { network.setStreet(result.street); }
            if (network.getCity() == null && result.city != null) { 
                network.setCity(result.city); 
                cityChanged = true;
            }
            if (network.getRegion() == null && result.region != null) { network.setRegion(result.region); }
            if (network.getCountry() == null && result.country != null) { network.setCountry(result.country); }
        }
        
        network.setNeedsNominatimUpdate(false);
        networkRepository.save(network);
        
        if (cityChanged) {
            // Update citiesCovered for users who uploaded this network
            List<Long> userIds = uploadRepository.findUserIdsByNetworkId(network.getId());
            for (Long userId : userIds) {
                userRepository.findById(userId).ifPresent(user -> {
                    int cities = uploadRepository.findDistinctCitiesByUser(userId).size();
                    user.setCitiesCovered(cities);
                    userRepository.save(user);
                });
            }
        }
    }

    private GeoResult fetchFromNominatim(double lat, double lon) {
        try {
            String url = String.format(java.util.Locale.US, "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1", lat, lon);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ScanneroneBackend/1.0 (contact@scannerone.local)"); // Nominatim requires a user agent with contact info
            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode address = root.path("address");
                
                if (!address.isMissingNode()) {
                    GeoResult geo = new GeoResult();
                    geo.street = address.path("road").asText(null);
                    geo.city = address.path("city").asText(address.path("town").asText(address.path("village").asText(null)));
                    geo.region = address.path("state").asText(null);
                    geo.country = address.path("country").asText(null);
                    return geo;
                }
            }
        } catch (Exception e) {
            log.error("Nominatim API error: {}", e.getMessage());
        }
        return null; // Return null if failed so we don't cache failures permanently
    }

    private static class GeoResult {
        String street;
        String city;
        String region;
        String country;
    }
}
