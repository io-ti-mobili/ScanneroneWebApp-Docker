package com.scannerone.repository;

import com.scannerone.entity.User;
import com.scannerone.entity.WifiNetwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDeviceToken(String deviceToken);

    Optional<User> findByDeviceTokenAndPassword(String deviceToken, String password);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // Leaderboard globale per score
    @Query("SELECT u FROM User u ORDER BY u.score DESC, u.uniqueDiscovered DESC, u.id ASC LIMIT :limit OFFSET :offset")
    List<User> findTopByScore(@Param("limit") int limit, @Param("offset") int offset);

    // Leaderboard per reti uniche scoperte
    @Query("SELECT u FROM User u ORDER BY u.uniqueDiscovered DESC, u.score DESC, u.id ASC LIMIT :limit OFFSET :offset")
    List<User> findTopByUniqueDiscovered(@Param("limit") int limit, @Param("offset") int offset);

    // Leaderboard per città coperte
    @Query("SELECT u FROM User u ORDER BY u.citiesCovered DESC, u.uniqueDiscovered DESC, u.id ASC LIMIT :limit OFFSET :offset")
    List<User> findTopByCitiesCovered(@Param("limit") int limit, @Param("offset") int offset);

    // Rank di un singolo utente per score (usato nella pagina profilo)
    @Query("""
        SELECT COUNT(u) + 1 FROM User u
        WHERE u.score > (SELECT u2.score FROM User u2 WHERE u2.id = :userId)
           OR (u.score = (SELECT u2.score FROM User u2 WHERE u2.id = :userId) AND u.uniqueDiscovered > (SELECT u2.uniqueDiscovered FROM User u2 WHERE u2.id = :userId))
           OR (u.score = (SELECT u2.score FROM User u2 WHERE u2.id = :userId) AND u.uniqueDiscovered = (SELECT u2.uniqueDiscovered FROM User u2 WHERE u2.id = :userId) AND u.id < :userId)
    """)
    long findRankByScore(@Param("userId") long userId);

    @Query("""
        SELECT u FROM User u
        WHERE (:country IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.firstSeenBy = u AND n.country = :country))
          AND (:region IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.firstSeenBy = u AND n.region = :region))
          AND (:city IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.firstSeenBy = u AND n.city = :city))
        ORDER BY u.score DESC, u.uniqueDiscovered DESC, u.id ASC LIMIT :limit OFFSET :offset
    """)
    List<User> findTopByScoreFiltered(
            @Param("country") String country, 
            @Param("region") String region, 
            @Param("city") String city, 
            @Param("limit") int limit, 
            @Param("offset") int offset);

    @Query("""
        SELECT u FROM User u
        WHERE (:country IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.firstSeenBy = u AND n.country = :country))
          AND (:region IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.region = :region AND n.firstSeenBy = u))
          AND (:city IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.city = :city AND n.firstSeenBy = u))
        ORDER BY u.uniqueDiscovered DESC, u.score DESC, u.id ASC LIMIT :limit OFFSET :offset
    """)
    List<User> findTopByUniqueDiscoveredFiltered(
            @Param("country") String country, 
            @Param("region") String region, 
            @Param("city") String city, 
            @Param("limit") int limit, 
            @Param("offset") int offset);

    @Query("""
        SELECT u FROM User u
        WHERE (:country IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.firstSeenBy = u AND n.country = :country))
          AND (:region IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.region = :region AND n.firstSeenBy = u))
          AND (:city IS NULL OR EXISTS (SELECT 1 FROM WifiNetwork n WHERE n.city = :city AND n.firstSeenBy = u))
        ORDER BY u.citiesCovered DESC, u.uniqueDiscovered DESC, u.id ASC LIMIT :limit OFFSET :offset
    """)
    List<User> findTopByCitiesCoveredFiltered(
            @Param("country") String country, 
            @Param("region") String region, 
            @Param("city") String city, 
            @Param("limit") int limit, 
            @Param("offset") int offset);
}
