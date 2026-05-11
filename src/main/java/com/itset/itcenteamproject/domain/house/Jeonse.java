package com.itset.itcenteamproject.domain.house;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "jeonses")
public class Jeonse {
    @Id
    private Long id;
    @Column(nullable = false)
    private Integer dongCode;
    @Column(nullable = false)
    private Integer jeonseRent;
}