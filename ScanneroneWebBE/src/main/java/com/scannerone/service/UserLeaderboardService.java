package com.scannerone.service;

import com.scannerone.dto.LeaderboardEntryDto;
import com.scannerone.entity.User;
import com.scannerone.repository.UserRepository;
import com.scannerone.repository.WifiNetworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserLeaderboardService {

    private final UserRepository userRepository;
    private final WifiNetworkRepository wifiNetworkRepository;

    public List<LeaderboardEntryDto> getGlobalLeaderboard(String country, String region, String city, int limit, int offset) {
        List<User> users = (country == null && region == null && city == null) 
                ? userRepository.findTopByScore(limit, offset)
                : userRepository.findTopByScoreFiltered(country, region, city, limit, offset);
        return mapToDto(users, offset);
    }

    public List<LeaderboardEntryDto> getDiscoveryLeaderboard(String country, String region, String city, int limit, int offset) {
        List<User> users = (country == null && region == null && city == null)
                ? userRepository.findTopByUniqueDiscovered(limit, offset)
                : userRepository.findTopByUniqueDiscoveredFiltered(country, region, city, limit, offset);
        return mapToDto(users, offset);
    }

    public List<LeaderboardEntryDto> getTravelerLeaderboard(int limit, int offset) {
        List<User> users = userRepository.findTopByCitiesCovered(limit, offset);
        return mapToDto(users, offset);
    }

    public long getUserRank(Long userId) {
        return userRepository.findRankByScore(userId);
    }

    private List<LeaderboardEntryDto> mapToDto(List<User> users, int offset) {
        return IntStream.range(0, users.size())
                .mapToObj(i -> {
                    User u = users.get(i);
                    LeaderboardEntryDto dto = new LeaderboardEntryDto();
                    dto.setRank(offset + i + 1);
                    dto.setUserId(u.getId());
                    dto.setUsername(u.getUsername());
                    dto.setDeviceToken(u.getDeviceToken());
                    dto.setScore(u.getScore());
                    dto.setUniqueDiscovered(u.getUniqueDiscovered());
                    dto.setCitiesCovered(u.getCitiesCovered());
                    dto.setAvgAccuracy(wifiNetworkRepository.avgAccuracyForUser(u.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
