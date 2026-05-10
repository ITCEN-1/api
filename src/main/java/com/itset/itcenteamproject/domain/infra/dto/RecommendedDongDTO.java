package com.itset.itcenteamproject.domain.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RecommendedDongDTO {

    private Integer ranking;   // 순위
    private Integer dongCode;  // 법정동 코드
    private String dongName;   // 동 이름
    private Double latitude;   // 위도
    private Double longitude;  // 경도
    private Double score;      // 계산 점수
    private String message;    // 보조 설명
}
