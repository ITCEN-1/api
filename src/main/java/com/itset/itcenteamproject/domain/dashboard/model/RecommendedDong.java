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

    //score 기준 내림차순 정렬 (점수 높은게 1등이므로), null은 마지막으로
    @Override
    public int compareTo(RecommendedDong o) {
        if (this.score == null && o.getScore() == null) return 0;
        if (this.score == null) return 1;
        if (o.getScore() == null) return -1;
        return o.getScore().compareTo(this.score);
    }
}
