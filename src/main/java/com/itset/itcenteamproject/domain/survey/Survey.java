package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "surveys")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workplaceAddress;
    private Integer jeonseMin;
    private Integer jeonseMax;
    private Integer monthlyMin;
    private Integer monthlyMax;

    @Builder
    public Survey(String workplaceAddress,
                  Integer jeonseMin,
                  Integer jeonseMax,
                  Integer monthlyMin,
                  Integer monthlyMax,
                  Integer depositMin,
                  Integer depositMax,
                  PreferenceLevel preferenceLargeStore,
                  PreferenceLevel preferenceHospital,
                  PreferenceLevel preferenceSubway,
                  PreferenceLevel preferenceLibrary,
                  User user) {
        this.workplaceAddress = workplaceAddress;
        this.jeonseMin = jeonseMin;
        this.jeonseMax = jeonseMax;
        this.monthlyMin = monthlyMin;
        this.monthlyMax = monthlyMax;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.preferenceLargeStore = preferenceLargeStore;
        this.preferenceHospital = preferenceHospital;
        this.preferenceSubway = preferenceSubway;
        this.preferenceLibrary = preferenceLibrary;
        this.user = user;
    }

    private Integer depositMin;
    private Integer depositMax;
    @Enumerated(EnumType.STRING)
    private PreferenceLevel preferenceLargeStore;
    @Enumerated(EnumType.STRING)
    private PreferenceLevel preferenceHospital;
    @Enumerated(EnumType.STRING)
    private PreferenceLevel preferenceSubway;
    @Enumerated(EnumType.STRING)
    private PreferenceLevel preferenceLibrary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)//FK 컬럼명
    private User user;

    @OneToMany(mappedBy = "survey" , cascade = CascadeType.ALL,orphanRemoval = true)
    private List<SurveySelectedDistrict>  surveySelectedDistrictList = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;
}
