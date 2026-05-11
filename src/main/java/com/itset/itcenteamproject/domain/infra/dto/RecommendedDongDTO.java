package com.itset.itcenteamproject.domain.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RecommendedDongDTO {

    private Integer ranking;
    private Integer dongCode;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private Double score;
    private String message;

    // 추가: 인프라별 개수
    private Long subwayCount;
    private Long hospitalCount;
    private Long libraryCount;
    private Long largeStoreCount;

    // 밀도 별 개수(개수 / 동 면적)
    private Double subwayDensity;
    private Double hospitalDensity;
    private Double libraryDensity;
    private Double largeStoreDensity;
}
