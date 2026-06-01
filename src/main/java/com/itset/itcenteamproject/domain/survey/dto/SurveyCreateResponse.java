package com.itset.itcenteamproject.domain.survey.dto;

import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SurveyCreateResponse {
    private Long surveyId;
    private String workPlaceAddress;
    private Integer jeonseMin;
    private Integer jeonseMax;
    private Integer monthlyMin;
    private Integer monthlyMax;
    private Integer depositMin;
    private Integer depositMax;
    private PreferenceLevel preferenceLargeStore;
    private PreferenceLevel preferenceHospital;
    private PreferenceLevel preferenceSubway;
    private PreferenceLevel preferenceLibrary;
    private List<String> selectedDistricts;
    private String submittedAt;

    protected SurveyCreateResponse() {}

    public static SurveyCreateResponse from(Survey survey) {
        return SurveyCreateResponse.builder()
                .surveyId(survey.getId())
                .workPlaceAddress(survey.getWorkplaceAddress())
                .jeonseMin(survey.getJeonseMin())
                .jeonseMax(survey.getJeonseMax())
                .monthlyMin(survey.getMonthlyMin())
                .monthlyMax(survey.getMonthlyMax())
                .depositMin(survey.getDepositMin())
                .depositMax(survey.getDepositMax())
                .preferenceLargeStore(survey.getPreferenceLargeStore())
                .preferenceHospital(survey.getPreferenceHospital())
                .preferenceSubway(survey.getPreferenceSubway())
                .preferenceLibrary(survey.getPreferenceLibrary())
                .selectedDistricts(survey.getSurveySelectedDistrictList().stream().map(s -> s.getDistrictName()).toList())
                .submittedAt(survey.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }
}
