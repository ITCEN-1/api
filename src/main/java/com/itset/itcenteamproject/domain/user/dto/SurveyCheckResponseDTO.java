package com.itset.itcenteamproject.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SurveyCheckResponseDTO {
    private boolean surveyCompleted;
    private String redirectPath;
}


