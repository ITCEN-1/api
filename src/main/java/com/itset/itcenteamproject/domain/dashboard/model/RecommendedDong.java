package com.itset.itcenteamproject.domain.dashboard.model;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedDong implements Comparable<RecommendedDong>{
    private Integer ranking;
    private Integer dongCode;
    private String districtName;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private BigDecimal score;
    private Integer commuteTime;
    private String message;

    //score 기준 내림차순 정렬 (점수 높은게 1등이므로)
    @Override
    public int compareTo(RecommendedDong o) {
        return o.getScore().compareTo(this.getScore());
        // o.getScore() - this.getScore() 결과로
        // o > this : o가 앞으로
        // o < this : o가 뒤로
    }
}
