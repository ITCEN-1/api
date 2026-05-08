package com.itset.itcenteamproject.domain.survey;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "survey_selected_districts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SurveySelectedDistrict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="survey_id")
    private Survey survey;

    @Column(nullable = false, length = 20)
    private String districtName;

    @Builder
    public SurveySelectedDistrict(Survey survey, String districtName) {
        this.survey = survey;
        this.districtName = districtName;
    }
}
