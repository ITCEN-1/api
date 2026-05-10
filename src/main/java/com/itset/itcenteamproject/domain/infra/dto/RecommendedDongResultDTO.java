package com.itset.itcenteamproject.domain.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class RecommendedDongResultDTO {

    // 예: top10 결과
    private List<RecommendedDongDTO> dongs;
}
