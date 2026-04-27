package com.scannerone.repository;


import com.scannerone.entity.NetworkUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NetworkUploadRepository extends JpaRepository<NetworkUpload, Long> {

    boolean existsByUserIdAndNetworkId(long userId, long networkId);

    // Tutte le città distinte toccate da un utente (per aggiornare citiesCovered)
    @Query("""
        SELECT DISTINCT nu.network.city FROM NetworkUpload nu
        WHERE nu.user.id = :userId AND nu.network.city IS NOT NULL
    """)
    List<String> findDistinctCitiesByUser(@Param("userId") long userId);

    // Upload giornalieri per utente (per timeline del profilo)
    @Query("""
        SELECT DATE(nu.uploadedAt), COUNT(nu) FROM NetworkUpload nu
        WHERE nu.user.id = :userId AND nu.uploadedAt BETWEEN :from AND :to
        GROUP BY DATE(nu.uploadedAt) ORDER BY DATE(nu.uploadedAt)
    """)
    List<Object[]> countDailyUploadsByUser(
            @Param("userId") long userId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Scoperte giornaliere globali (per la dashboard)
    @Query("""
        SELECT DATE(nu.uploadedAt), COUNT(nu) FROM NetworkUpload nu
        WHERE nu.isFirstDiscovery = true AND nu.uploadedAt BETWEEN :from AND :to
        GROUP BY DATE(nu.uploadedAt) ORDER BY DATE(nu.uploadedAt)
    """)
    List<Object[]> countDailyDiscoveries(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Punti guadagnati per giorno per utente (per grafico andamento score)
    @Query("""
        SELECT DATE(nu.uploadedAt), SUM(nu.pointsAwarded) FROM NetworkUpload nu
        WHERE nu.user.id = :userId
        GROUP BY DATE(nu.uploadedAt) ORDER BY DATE(nu.uploadedAt)
    """)
    List<Object[]> sumDailyPointsByUser(@Param("userId") long userId);

    @Query(value = "SELECT CAST(uploaded_at AS DATE) as d, COUNT(*) FROM network_uploads GROUP BY d ORDER BY d", nativeQuery = true)
    List<Object[]> countGlobalDailyUploads();
}