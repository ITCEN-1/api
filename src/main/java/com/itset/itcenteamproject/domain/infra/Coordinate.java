package com.itset.itcenteamproject.domain.infra;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *  위도 경도를 쌍으로 묶는 객체, 불변 객체이므로 final 선언
 *  이런 경우엔 class 대신 record로 사용한다고도 하는데 잘 몰라서 이걸로 썼음
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public final class Coordinate {
    private final Double longitude;
    private final Double latitude;
}
