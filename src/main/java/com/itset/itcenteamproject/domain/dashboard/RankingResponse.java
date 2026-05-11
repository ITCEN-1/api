package com.itset.itcenteamproject.domain.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankingResponse {
    private Integer ranking; // 최종 순위
    private Integer dongCode; // 법정동 코드
    private String dongName; // 법정동 이름
    private Double latitude;
    private Double longitude;
    private BigDecimal score; // 최종 점수
}
