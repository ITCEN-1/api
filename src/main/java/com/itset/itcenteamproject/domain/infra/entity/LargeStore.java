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
@Table(name = "large_stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LargeStore {

    @Id
    @Column(name = "id")
    private Long id; // 대규모점포 PK

    @Column(name = "name", nullable = false, length = 30)
    private String name; // 점포명

    @Column(name = "dong_code", nullable = false)
    private Integer dongCode; // 소속 법정동 코드

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;
}
