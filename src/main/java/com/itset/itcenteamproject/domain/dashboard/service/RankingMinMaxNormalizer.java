package com.itset.itcenteamproject.domain.dashboard.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RankingMinMaxNormalizer {

    // 기존 하위호환 메서드: 실제 size를 maxSize로 사용

    /**
     * 순위 기반 Min-Max 정규화
     * @param ranking 1-based rank
     * @param baseScore 기본 점수
     * @param maxSize 실제 데이터 수 (ranking의 상한 검증용)
     * @param maxSize 정규화에 사용할 최대 크기 (다른 계산기와 스케일을 맞추기 위해 사용)
     * @return baseScore 에 더할 정규화 점수(0..100)
     */
    public BigDecimal getMinMaxNormalizedScore(int ranking, BigDecimal baseScore, int maxSize) {
        if (maxSize <= 1) {
            return BigDecimal.valueOf(100).add(baseScore);
        }
        if (ranking > maxSize) {
            ranking = maxSize;
        }

        BigDecimal rankIndex = BigDecimal.valueOf(ranking - 1);
        BigDecimal denom = BigDecimal.valueOf(maxSize - 1);
        BigDecimal ratio = rankIndex.divide(denom, 6, RoundingMode.HALF_UP); // 0..1
        BigDecimal score = BigDecimal.valueOf(100).multiply(BigDecimal.ONE.subtract(ratio));
        return baseScore.add(score);
    }
}
