package com.itset.itcenteamproject.domain.dashboard.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RankingMinMaxNormalizer {

    public BigDecimal getMinMaxNormalizedScore(int ranking, BigDecimal baseScore, int size) {
        if (size <= 1) {
            return BigDecimal.valueOf(100);
        }
        // Min-Max 정규화
        // ranking이 1등이면 100점, size등이면 0점이 되도록 계산
        // BigDecimal로 나눗셈하여 소수 손실을 방지
        BigDecimal rankIndex = BigDecimal.valueOf(ranking - 1);
        BigDecimal denom = BigDecimal.valueOf(size - 1);
        BigDecimal ratio = rankIndex.divide(denom, 6, RoundingMode.HALF_UP); // 0..1
        BigDecimal score = BigDecimal.valueOf(100).multiply(BigDecimal.ONE.subtract(ratio));
        return baseScore.add(score);
    }
}
