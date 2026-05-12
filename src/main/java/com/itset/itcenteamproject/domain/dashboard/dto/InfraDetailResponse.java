package com.itset.itcenteamproject.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 요소별 세부정보 응답 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfraDetailResponse {
    private Long surveyId;
    private Integer dongCode;
    private InfraType type;
    private List<InfraItemResponse> items;
}
