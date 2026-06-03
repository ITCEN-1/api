package com.itset.itcenteamproject.domain.infra.service;

import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import org.springframework.stereotype.Service;

@Service
public class PreferenceWeightService {

    // 설문 선호도(enum)를 계산용 가중치(double)로 변환
    public double toWeight(PreferenceLevel level) {
        if (level == null) return 0.0; // null 방어

        return switch (level) {
            case HIGH -> 1.0;
            case MIDDLE -> 0.66;
            case LOW -> 0.33;
        };
    }
}