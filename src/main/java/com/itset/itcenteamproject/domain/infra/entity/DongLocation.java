package com.itset.itcenteamproject.domain.infra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dong_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DongLocation {

    @Id
    @Column(name = "dong_code")
    private Integer dongCode; // 법정동 코드(PK)

    @Column(name = "district_name", nullable = false, length = 20)
    private String districtName; // 구 이름

    @Column(name = "dong_name", nullable = false, length = 20)
    private String dongName; // 동 이름

    @Column(name = "longitude", nullable = false)
    private Double longitude; // 경도

    @Column(name = "latitude", nullable = false)
    private Double latitude; // 위도

    @Column(name = "dong_area", nullable = false)
    private Double dongArea; // 동 면적 (밀도 계산 분모)
}
