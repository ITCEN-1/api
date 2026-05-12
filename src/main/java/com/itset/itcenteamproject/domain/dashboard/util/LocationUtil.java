package com.itset.itcenteamproject.domain.dashboard.util;

import com.itset.itcenteamproject.domain.infra.Coordinate;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationUtil {
    private final DongLocationRepository dongLocationRepository;

    // 동 코드를 위도 경도로 변환
    public Coordinate dongCodeToCoordinate(Integer dongCode){
        DongLocation dongLocation = dongLocationRepository.findById(dongCode)//dongCode가 PK이므로 굳이 findByDongCode를 따로 안만듦( @Id 어노테이션이라 자동인식)
                .orElseThrow(()-> new CustomException(ErrorCode.INVALID_DONG_CODE));
        return new Coordinate(dongLocation.getLongitude(),dongLocation.getLatitude());
    }

}
