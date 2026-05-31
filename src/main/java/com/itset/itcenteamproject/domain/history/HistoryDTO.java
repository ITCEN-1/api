package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.survey.dto.SurveyDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class HistoryDTO {
    private SurveyDTO surveyDto;
    private List<RecommendedDong> rankings;
}
