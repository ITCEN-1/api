package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.survey.SurveyDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class HistoryDTO {
    private SurveyDTO surveyDto;
    private List<HistoryItemDTO> rankings;
}
