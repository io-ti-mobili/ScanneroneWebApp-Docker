package com.scannerone.repository;

import com.scannerone.entity.WifiNetwork;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WifiNetworkRepository extends JpaRepository<WifiNetwork, Long> {

    Optional<WifiNetwork> findByBssid(String bssid);

    List<WifiNetwork> findByBssidIn(java.util.Collection<String> bssids);

    boolean existsByBssid(String bssid);

    // --- Mappa ---

    @Query("""
                SELECT n FROM WifiNetwork n
                WHERE n.latitude BETWEEN :minLat AND :maxLat
                  AND n.longitude BETWEEN :minLon AND :maxLon
                  AND n.latitude IS NOT NULL AND n.longitude IS NOT NULL
            """)
    Page<WifiNetwork> findByBoundingBox(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLon") double minLon, @Param("maxLon") double maxLon,
            Pageable pageable);

    @Query("""
                SELECT n FROM WifiNetwork n
                WHERE n.latitude BETWEEN :minLat AND :maxLat
                  AND n.longitude BETWEEN :minLon AND :maxLon
                  AND n.latitude IS NOT NULL AND n.longitude IS NOT NULL
                  AND n.security IN :securities
            """)
    Page<WifiNetwork> findByBoundingBoxAndSecurity(
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLon") double minLon, @Param("maxLon") double maxLon,
            @Param("securities") List<String> securities,
            Pageable pageable);

    // --- Top geografici ---

    @Query("""
                SELECT n.city, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.city IS NOT NULL GROUP BY n.city ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopCitiesByNetworkCount(@Param("limit") int limit);

    @Query("""
                SELECT n.region, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.region IS NOT NULL GROUP BY n.region ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopRegionsByNetworkCount(@Param("limit") int limit);

    @Query("""
                SELECT n.country, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.country IS NOT NULL GROUP BY n.country ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopCountriesByNetworkCount(@Param("limit") int limit);

    @Query("""
                SELECT n.region, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.country = :country AND n.region IS NOT NULL GROUP BY n.region ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopRegionsByCountry(@Param("country") String country, @Param("limit") int limit);

    @Query("""
                SELECT n.city, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.region = :region AND n.city IS NOT NULL GROUP BY n.city ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopCitiesByRegion(@Param("region") String region, @Param("limit") int limit);

    @Query("""
                SELECT n.city, COUNT(n) AS cnt FROM WifiNetwork n
                WHERE n.city IS NOT NULL AND n.security = 'WPA3'
                GROUP BY n.city ORDER BY cnt DESC LIMIT :limit
            """)
    List<Object[]> findTopCitiesByWpa3Count(@Param("limit") int limit);

    @Query("""
                SELECT n.city, (SUM(CASE WHEN n.security = 'OPEN' THEN 1 ELSE 0 END) * 1.0 / COUNT(n)) AS openRatio
                FROM WifiNetwork n
                WHERE n.city IS NOT NULL
                GROUP BY n.city
                HAVING COUNT(n) > 5
                ORDER BY openRatio DESC LIMIT :limit
            """)
    List<Object[]> findTopCitiesByOpenRatio(@Param("limit") int limit);

    // --- Stats globali ---

    // --- Stats globali filtrate ---

    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.security = 'OPEN' AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    long countOpenNetworks(@Param("country") String country, @Param("region") String region);

    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.security = 'WPA3' AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    long countWpa3Networks(@Param("country") String country, @Param("region") String region);

    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.frequencyBand > 2.3 AND n.frequencyBand < 2.5 AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    long countBand24Networks(@Param("country") String country, @Param("region") String region);

    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.frequencyBand > 4.9 AND n.frequencyBand < 5.1 AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    long countBand5Networks(@Param("country") String country, @Param("region") String region);

    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.frequencyBand > 5.9 AND n.frequencyBand < 6.1 AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    long countBand6Networks(@Param("country") String country, @Param("region") String region);

    @Query("SELECT n.security, COUNT(n) FROM WifiNetwork n WHERE (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) GROUP BY n.security")
    List<Object[]> countBySecurity(@Param("country") String country, @Param("region") String region);

    @Query("SELECT n.category, COUNT(n) FROM WifiNetwork n WHERE (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) GROUP BY n.category")
    List<Object[]> countNetworksByCategory(@Param("country") String country, @Param("region") String region);

    @Query(value = """
                SELECT CAST(first_seen_at AS DATE) as d, COUNT(*)
                FROM wifi_networks
                WHERE (:country IS NULL OR country = :country) AND (:region IS NULL OR region = :region)
                GROUP BY d ORDER BY d
            """, nativeQuery = true)
    List<Object[]> countNetworksByDate(@Param("country") String country, @Param("region") String region);

    @Query("SELECT AVG(n.estAccuracy) FROM WifiNetwork n WHERE n.estAccuracy IS NOT NULL AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)")
    Double avgAccuracy(@Param("country") String country, @Param("region") String region);

    @Query("SELECT COUNT(DISTINCT n.city) FROM WifiNetwork n WHERE (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) AND n.city IS NOT NULL")
    long countDistinctCities(@Param("country") String country, @Param("region") String region);

    @Query("SELECT n.city, COUNT(n) AS cnt FROM WifiNetwork n WHERE n.city IS NOT NULL AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) GROUP BY n.city ORDER BY cnt DESC LIMIT :limit")
    List<Object[]> findTopCitiesByNetworkCountFiltered(@Param("country") String country, @Param("region") String region,
            @Param("limit") int limit);

    @Query(value = "SELECT n.city, COUNT(*) AS cnt FROM wifi_networks n WHERE n.city IS NOT NULL AND n.security = 'WPA3' AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) GROUP BY n.city ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCitiesByWpa3CountFiltered(@Param("country") String country, @Param("region") String region,
            @Param("limit") int limit);

    @Query(value = "SELECT n.country, COUNT(*) AS cnt FROM wifi_networks n WHERE n.country IS NOT NULL AND n.security = 'WPA3' GROUP BY n.country ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopCountriesByWpa3Count(@Param("limit") int limit);

    @Query(value = "SELECT n.region, COUNT(*) AS cnt FROM wifi_networks n WHERE n.country = :country AND n.region IS NOT NULL AND n.security = 'WPA3' GROUP BY n.region ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopRegionsByWpa3Count(@Param("country") String country, @Param("limit") int limit);

    @Query(value = """
                SELECT n.city, (SUM(CASE WHEN n.security = 'OPEN' THEN 1 ELSE 0 END) * 1.0 / COUNT(*)) AS openRatio
                FROM wifi_networks n
                WHERE n.city IS NOT NULL AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region)
                GROUP BY n.city
                HAVING COUNT(*) > 5
                ORDER BY openRatio DESC LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopCitiesByOpenRatioFiltered(@Param("country") String country, @Param("region") String region,
            @Param("limit") int limit);

    @Query("""
                SELECT n.country, (SUM(CASE WHEN n.security = 'OPEN' THEN 1 ELSE 0 END) * 1.0 / COUNT(n)) AS openRatio
                FROM WifiNetwork n
                WHERE n.country IS NOT NULL
                GROUP BY n.country
                HAVING COUNT(n) > 10
                ORDER BY openRatio DESC LIMIT :limit
            """)
    List<Object[]> findTopCountriesByOpenRatio(@Param("limit") int limit);

    @Query("""
                SELECT n.region, (SUM(CASE WHEN n.security = 'OPEN' THEN 1 ELSE 0 END) * 1.0 / COUNT(n)) AS openRatio
                FROM WifiNetwork n
                WHERE n.country = :country AND n.region IS NOT NULL
                GROUP BY n.region
                HAVING COUNT(n) > 5
                ORDER BY openRatio DESC LIMIT :limit
            """)
    List<Object[]> findTopRegionsByOpenRatio(@Param("country") String country, @Param("limit") int limit);

    @Query("SELECT COUNT(DISTINCT n.country) FROM WifiNetwork n WHERE n.country IS NOT NULL")
    long countDistinctCountries();

    // --- Stats per utente ---

    @Query("SELECT n.security, COUNT(n) FROM WifiNetwork n WHERE n.firstSeenBy.id = :userId GROUP BY n.security")
    List<Object[]> countBySecurityForUser(@Param("userId") long userId);

    @Query("SELECT COUNT(DISTINCT n.country) FROM WifiNetwork n WHERE n.firstSeenBy.id = :userId AND n.country IS NOT NULL")
    long countDistinctCountriesForUser(@Param("userId") long userId);

    @Query("SELECT AVG(n.estAccuracy) FROM WifiNetwork n WHERE n.firstSeenBy.id = :userId AND n.estAccuracy IS NOT NULL")
    Double avgAccuracyForUser(@Param("userId") long userId);

    @Query("""
                SELECT COUNT(n) FROM WifiNetwork n
                WHERE n.firstSeenBy.id = :userId
                  AND n.street IS NOT NULL AND n.city IS NOT NULL
                  AND n.region IS NOT NULL AND n.country IS NOT NULL
            """)
    long countFullGeoForUser(@Param("userId") long userId);

    @Query("SELECT n.security, COUNT(n) FROM WifiNetwork n GROUP BY n.security")
    List<Object[]> countNetworksBySecurity();

    @Query("SELECT n.city, COUNT(n) AS cnt FROM WifiNetwork n WHERE n.country = :country AND n.city IS NOT NULL GROUP BY n.city ORDER BY cnt DESC LIMIT :limit")
    List<Object[]> findTopCitiesByCountry(@Param("country") String country, @Param("limit") int limit);

    @Query("SELECT DISTINCT n.country FROM WifiNetwork n WHERE n.country IS NOT NULL ORDER BY n.country")
    List<String> findDistinctCountries();

    // --- Nominatim ---
    List<WifiNetwork> findTop50ByNeedsNominatimUpdateTrue();
    
    @Query("SELECT COUNT(n) FROM WifiNetwork n WHERE n.city IS NULL AND n.latitude IS NOT NULL AND n.needsNominatimUpdate = false")
    long countFailedNominatimNetworks();

    // --- Geografiche paginate ---
    @Query(value = "SELECT DISTINCT n.country FROM wifi_networks n WHERE n.country IS NOT NULL ORDER BY n.country LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<String> findDistinctCountriesPaginated(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT DISTINCT n.region FROM wifi_networks n WHERE n.region IS NOT NULL AND (:country IS NULL OR n.country = :country) ORDER BY n.region LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<String> findDistinctRegionsPaginated(@Param("country") String country, @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = "SELECT DISTINCT n.city FROM wifi_networks n WHERE n.city IS NOT NULL AND (:country IS NULL OR n.country = :country) AND (:region IS NULL OR n.region = :region) ORDER BY n.city LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<String> findDistinctCitiesPaginated(@Param("country") String country, @Param("region") String region,
            @Param("limit") int limit, @Param("offset") int offset);
}
