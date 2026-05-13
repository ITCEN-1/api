package com.itset.itcenteamproject.domain.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.infra.Coordinate;
import com.itset.itcenteamproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommuteScoreCalculator {

    private final LocationService locationUtil;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OdsayApiKeys odsayApiKeys;
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

            // 오디세이로 통근시간 가져오기
            int commuteMinutes=getCommuteMinutesByOdsay(workplaceCoordinate,rd.getDongCode(), odsayApiKeys.getNextKey());

            // 통근시간으로 점수 산정해서 기존 점수에 더하기
            BigDecimal newScore = rd.getScore().add( //Decimal끼리는 + 연산 말고 add 메소드 사용함
                    convertMinutesToScore(commuteMinutes));

            // 점수 추가 이력 추가
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
     * 오디세이 API를 호출하여 좌표간 통근시간(분)을 반환합니다
     * @param workplaceCoordinate
     * @param destinationDongCode
     * @param apiKey
     * @return 통근시간(분)
     */
    public int getCommuteMinutesByOdsay(Coordinate workplaceCoordinate,Integer destinationDongCode,String apiKey){

        // 유효성 검증
        validateDongCode(destinationDongCode);

        Coordinate destinationCoor = locationUtil.dongCodeToCoordinate(destinationDongCode);

        // 요청 uri 정의
        String uriString = "https://api.odsay.com/v1/api/searchPubTransPathT"
                + "?apiKey=" + apiKey
                + "&lang=0"
                + "&SX=" + workplaceCoordinate.getLongitude()
                + "&SY=" + workplaceCoordinate.getLatitude()
                + "&EX=" + destinationCoor.getLongitude()
                + "&EY=" + destinationCoor.getLatitude()
                + "&OPT=0"
                + "&SearchType=0"
                + "&SearchPathType=0";

        String response=null;
        try{
            response = restClient.get()
                .uri(URI.create(uriString))
                .retrieve()//http 요청을 실행
                .body(String.class);//응답 body를 String 으로 변환

        }catch (RestClientException e){
            throw new CustomException(ODSAY_API_ERROR);
        }

        return convertOdsayResponseToTotalMinutes(response);
    }

    // 오디세이 응답 JSON 에서 가장 빠른 경로 소요시간을 파싱
    private int convertOdsayResponseToTotalMinutes(String response) {

        if (response == null || response.isBlank()) {
            log.error("[ODsay] 응답이 비어있음. response: '{}'", response);
            throw new CustomException(ODSAY_API_ERROR);
        }

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode pathArray = root.path("result").path("path");
            JsonNode firstPath = pathArray.path(0);
            JsonNode totalTime = firstPath.path("info").path("totalTime");

            if (pathArray.isMissingNode()) {
                log.error("[ODsay] 'result.path' 노드 없음. response: {}", response);
                throw new CustomException(ODSAY_PARSE_ERROR);
            }

            if (pathArray.isEmpty()) {
                log.error("[ODsay] 경로 결과가 0개. response: {}", response);
                throw new CustomException(ODSAY_PARSE_ERROR);
            }

            if (firstPath.isMissingNode()) {
                log.error("[ODsay] 첫 번째 경로(path[0]) 없음. response: {}", response);
                throw new CustomException(ODSAY_PARSE_ERROR);
            }

            if (!totalTime.isNumber()) {
                log.error("[ODsay] 'totalTime' 파싱 실패. totalTime 노드: {}, response: {}", totalTime, response);
                throw new CustomException(ODSAY_PARSE_ERROR);
            }

            int result = totalTime.asInt();
            log.debug("[ODsay] 파싱 성공. totalTime: {}분", result);
            return result;

        } catch (JsonProcessingException e) {
            log.error("[ODsay] JSON 파싱 실패. response: {}", response, e);
            throw new CustomException(ODSAY_PARSE_ERROR);
        }
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
