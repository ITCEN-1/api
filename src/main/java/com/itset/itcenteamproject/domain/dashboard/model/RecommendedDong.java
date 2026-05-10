package com.itset.itcenteamproject.domain.dashboard.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedDong {
    private Integer ranking;
    private Integer dongCode;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private BigDecimal score;
    private String message;
}