package com.itset.itcenteamproject.domain.house;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "wolses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Wolse {
    @Id
    private Long id;
    @Column(nullable = false)
    private Integer dongCode;
    @Column(nullable = false)
    private Integer deposit;
    @Column(nullable = false)
    private Integer monthlyRent;
}