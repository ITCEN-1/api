package com.itset.itcenteamproject.domain.survey.dto;

import com.itset.itcenteamproject.domain.survey.entity.SurveySelectedDistrict;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SurveySelectedDistrictDTO {
    private Long id;
    private String districtName;

    public static SurveySelectedDistrictDTO from(SurveySelectedDistrict district) {
        return SurveySelectedDistrictDTO.builder()
                .id(district.getId())
                .districtName(district.getDistrictName())
                .build();
    }
}
