package com.itset.itcenteamproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 동 요약(세부정보) 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DongDetailResponse {
    private Long surveyId;
    private Integer dongCode;
    private String dongName;
    private Double latitude;
    private Double longitude;

    // 인프라 개수
    private Long hospitalCount;
    private Long subwayCount;
    private Long libraryCount;
    private Long largeStoreCount;

    // 전/월세 매물 개수
    private Long jeonseCount;
    private Long wolseCount;
}
