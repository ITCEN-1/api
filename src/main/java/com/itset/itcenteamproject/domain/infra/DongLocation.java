package com.itset.itcenteamproject.domain.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Table(name = "dong_locations")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DongLocation {
    @Id
    @Column(name = "dong_code")
    private Integer dongCode;
    @Column(name = "district_name", nullable = false, length = 20)
    private String districtName;
    @Column(name = "dong_name", nullable = false, length = 20)
    private String dongName;
    @Column(nullable = false)
    private Double longitude;
    @Column(nullable = false)
    private Double latitude;
    @Column(name = "dong_area", nullable = false, precision = 11, scale = 9)
    private BigDecimal dongArea;
}
