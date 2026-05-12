package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.user.User;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyCreateRequest {

    //nullable
    private String workplaceAddress;

    //null체크는 SurveyService에서 검증
    @Min(value = 0)
    private Integer jeonseMin;
    @Min(value = 0)
    private Integer jeonseMax;
    @Min(value = 0)
    private Integer monthlyMin;
    @Min(value = 0)
    private Integer monthlyMax;
    @Min(value = 0)
    private Integer depositMin;
    @Min(value = 0)
    private Integer depositMax;

    @NotNull(message = "대형마트 선호도는 필수입니다.")
    private PreferenceLevel preferenceLargeStore;

    @NotNull(message = "병원 선호도는 필수입니다.")
    private PreferenceLevel preferenceHospital;

    @NotNull(message = "지하철 선호도는 필수입니다.")
    private PreferenceLevel preferenceSubway;

    @NotNull(message = "도서관 선호도는 필수입니다.")
    private PreferenceLevel preferenceLibrary;

    private List<String> selectedDistricts; //RequestDTO 이므로 클라이언트가 실제로 보내는 데이터 대로

    //surveySelectedDistrictList 별도로 저장
    public Survey toEntity(User user) {
        return Survey.builder()
                .user(user)
                .workplaceAddress(workplaceAddress)
                .jeonseMin(jeonseMin)
                .jeonseMax(jeonseMax)
                .monthlyMin(monthlyMin)
                .monthlyMax(monthlyMax)
                .depositMin(depositMin)
                .depositMax(depositMax)
                .preferenceLargeStore(preferenceLargeStore)
                .preferenceHospital(preferenceHospital)
                .preferenceSubway(preferenceSubway)
                .preferenceLibrary(preferenceLibrary)
                .build();
    }
}
