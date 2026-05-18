package com.itset.itcenteamproject.domain.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.dashboard.client.OdsayApiKeys;
import com.itset.itcenteamproject.domain.dashboard.client.OdsayClient;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.global.vo.Coordinate;
import com.itset.itcenteamproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommuteScoreCalculator {

    private final LocationService locationUtil;
    private final OdsayClient odsayClient;
    /**
     * workplaceDongCode(직장,학교 동) 를 기준으로 recommendedDongList(추천된 동) 에 있는 각 동 까지 걸리는 시간을
     * 조회하여 추가 점수를 부여하고 RecommendedDong.score에 가산하여 리턴합니다
     * @param workplaceCoordinate
     * @param recommendedDongList
     * @return 통근거리를 기준으로 점수가 추가된 addCommuteScoreDongList 리스트 반환
     */
    public List<RecommendedDong> calculate(Coordinate workplaceCoordinate, List<RecommendedDong> recommendedDongList){
        List<RecommendedDong> newRecommendedDong= new ArrayList<>();
        for(RecommendedDong rd: recommendedDongList){

            // 추천된 동 코드를 좌표로 변환
            Integer startingDongCode = rd.getDongCode();
            validateDongCode(startingDongCode);
            Coordinate startingDongCoordinate = locationUtil.dongCodeToCoordinate(startingDongCode);

            // 오디세이 API로 통근시간 가져오기
            String odsayResponse=odsayClient.getCommuteMinutes(startingDongCoordinate,workplaceCoordinate);
            int commuteMinutes=odsayClient.convertOdsayResponseToTotalMinutes(odsayResponse);

            // 통근시간으로 점수 산정해서 기존 점수에 더하기 (Decimal 은 + 대신 add)
            BigDecimal newScore = rd.getScore().add(convertMinutesToScore(commuteMinutes));

            // commute 점수 이력 추가
            String newMessage = rd.getMessage()+" commute: "+convertMinutesToScore(commuteMinutes);

            newRecommendedDong.add(RecommendedDong.builder()
                    .commuteTime(commuteMinutes)
                    .dongCode(rd.getDongCode())
                    .dongName(rd.getDongName())
                    .score(newScore)
                    .longitude(rd.getLongitude())
                    .latitude(rd.getLatitude())
                    .message(newMessage)
                    .build());

            // 429 에러를 막기 위해 각 요청마다 대기를 준다
            try {
                Thread.sleep(50); //300ms 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return newRecommendedDong;
    }

    /**
     * 통근시간(분)을 점수로 수치화하는 메소드입니다 산정 방식은 임의로 정했습니다
     * @param minutes
     * @return 점수
     */
    private BigDecimal convertMinutesToScore(int minutes) {
        //TODO: 팀 회의 후 수치값 적절하게 변경
        if (minutes <= 10) return BigDecimal.valueOf(50);
        if (minutes <= 20) return BigDecimal.valueOf(45);
        if (minutes <= 30) return BigDecimal.valueOf(40);
        if (minutes <= 45) return BigDecimal.valueOf(30);
        if (minutes <= 60) return BigDecimal.valueOf(20);
        if (minutes <= 90) return BigDecimal.valueOf(10);
        return BigDecimal.valueOf(10);
    }

    //유효성 검증
    private void validateDongCode(Integer dongCode) {
        if (dongCode == null) {
            throw new CustomException(INVALID_DONG_CODE);
        }
        if (dongCode < 1111010100 || dongCode > 1174011000) {
            throw new CustomException(INVALID_DONG_CODE);
        }
    }
}
