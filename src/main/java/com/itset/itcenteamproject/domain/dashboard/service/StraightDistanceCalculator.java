package com.itset.itcenteamproject.domain.dashboard.service;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.global.vo.Coordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StraightDistanceCalculator {

    private final RankingMinMaxNormalizer rankingMinMaxNormalizer;

    public List<RecommendedDong> calculateStraightDistanceScore(Coordinate destination, List<RecommendedDong> recommendedDongs) {
        if (destination == null) throw new IllegalArgumentException("destination cannot be null");

        // build list of distances
        List<DistanceEntry> distances = new ArrayList<>();
        for (RecommendedDong rd : recommendedDongs) {
            if (rd.getLatitude() == null || rd.getLongitude() == null) continue;
            double dkm = haversineDistanceKm(rd.getLatitude(), rd.getLongitude(), destination.getLatitude(), destination.getLongitude());
            distances.add(new DistanceEntry(rd.getDongCode(), dkm));
        }

        if (distances.isEmpty()) return recommendedDongs;

        // sort ascending by distance (shorter is better)
        distances.sort(Comparator.comparingDouble(d -> d.distanceKm));

        int size = distances.size();
        Map<Integer, Integer> rankMap = new HashMap<>();
        for (int i = 0; i < distances.size(); i++) {
            rankMap.put(distances.get(i).dongCode, i + 1);
        }

        // produce updated recommended dongs
        List<RecommendedDong> updatedList = recommendedDongs.stream()
                .filter(rd -> rankMap.containsKey(rd.getDongCode()))
                .map(rd -> {
                    int rank = rankMap.get(rd.getDongCode());
                    BigDecimal normalized = rankingMinMaxNormalizer.getMinMaxNormalizedScore(rank, BigDecimal.ZERO, size);
                    BigDecimal existing = rd.getScore() != null ? rd.getScore() : BigDecimal.ZERO;
                    double distanceKm = distances.stream().filter(e -> e.dongCode.equals(rd.getDongCode())).findFirst().map(e -> e.distanceKm).orElse(0.0);
                    String newMessage = rd.getMessage() + String.format(" distance: %.3fkm", distanceKm) + " distScore: " + normalized;
                    return RecommendedDong.builder()
                            .dongCode(rd.getDongCode())
                            .districtName(rd.getDistrictName())
                            .dongName(rd.getDongName())
                            .score(existing.add(normalized))
                            .longitude(rd.getLongitude())
                            .latitude(rd.getLatitude())
                            .commuteTime(rd.getCommuteTime())
                            .message(newMessage)
                            .build();
                })
                .sorted(Comparator.comparing(RecommendedDong::getScore).reversed())
                .collect(Collectors.toList());

        return getTop10RecommendedDongs(updatedList);
    }

    private static class DistanceEntry {
        Integer dongCode;
        double distanceKm;
        DistanceEntry(Integer dongCode, double distanceKm) { this.dongCode = dongCode; this.distanceKm = distanceKm; }
    }

    private double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    private List<RecommendedDong> getTop10RecommendedDongs(List<RecommendedDong> recommendedDongs) {

        return recommendedDongs.stream()
                .sorted(Comparator.comparing(RecommendedDong::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

}
