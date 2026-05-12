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
@Table(name = "hospitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @Column(name = "id")
    private Long id; // 병원 PK

    @Column(name = "name", nullable = false, length = 30)
    private String name; // 병원명

    @Column(name = "dong_code", nullable = false)
    private Integer dongCode; // 소속 법정동 코드
}
