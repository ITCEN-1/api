package com.itset.itcenteamproject.domain.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SurveyDTO {
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
    private List<SurveySelectedDistrictDTO> surveySelectedDistrictList;
    private String submittedAt;

    protected SurveyDTO() {}

    public static SurveyDTO from(Survey survey) {
        return SurveyDTO.builder()
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
                .surveySelectedDistrictList(survey.getSurveySelectedDistrictList().stream()
                        .map(SurveySelectedDistrictDTO::from)
                        .collect(Collectors.toList()))
                .submittedAt(survey.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }
}
