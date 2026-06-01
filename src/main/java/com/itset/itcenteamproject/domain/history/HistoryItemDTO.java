package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HistoryItemDTO {
    private Integer ranking;
    private Integer dongCode;
    private String districtName;
    private String dongName;
    private Double latitude;
    private Double longitude;
    private Integer commuteTime;

    public static HistoryItemDTO from(HistoryItem historyItem) {
        DongLocation dongLocation = historyItem.getDongLocation();

        return HistoryItemDTO.builder()
                .ranking(historyItem.getRanking())
                .dongCode(historyItem.getDongCode())
                .districtName(dongLocation.getDistrictName())
                .dongName(dongLocation.getDongName())
                .latitude(dongLocation.getLatitude())
                .longitude(dongLocation.getLongitude())
                .commuteTime(historyItem.getCommuteTime())
                .build();
    }
}
