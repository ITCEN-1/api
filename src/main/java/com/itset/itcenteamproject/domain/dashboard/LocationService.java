package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.infra.Coordinate;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.survey.SurveySelectedDistrict;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationService {
    private final DongLocationRepository dongLocationRepository;

    // 동 코드를 위도 경도로 변환
    public Coordinate dongCodeToCoordinate(Integer dongCode){
        DongLocation dongLocation = dongLocationRepository.findById(dongCode)//dongCode가 PK이므로 굳이 findByDongCode를 따로 안만듦( @Id 어노테이션이라 자동인식)
                .orElseThrow(()-> new CustomException(ErrorCode.INVALID_DONG_CODE));
        return new Coordinate(dongLocation.getLongitude(),dongLocation.getLatitude());
    }

    // 설문의 선택 구 리스트에 속한 법정동코드 리스트 반환
    public List<Integer> getDongCodesBySurvey(Survey survey){
        List<Integer> dongCodes=new ArrayList<>();
        List<SurveySelectedDistrict> districtList = survey.getSurveySelectedDistrictList();

        for(SurveySelectedDistrict dist:districtList){
            String districtName = dist.getDistrictName();
            List<Integer> dongCodesInDistrict = dongLocationRepository.findAllByDistrictName(districtName).stream()
                    .map(DongLocation::getDongCode).toList();
            dongCodes.addAll(dongCodesInDistrict);// addAll: 리스트에 리스트 더하기
        }
        return dongCodes;
    }
}
