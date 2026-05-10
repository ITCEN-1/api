package com.itset.itcenteamproject.domain.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.dashboard.util.LocationUtil;
import com.itset.itcenteamproject.domain.infra.Coordinate;
import com.itset.itcenteamproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class CommuteScoreCalculator {

    private final LocationUtil locationUtil;

    /**
     * workplaceDongCode(직장,학교 동) 를 기준으로 recommendedDongList(추천된 동) 에 있는 각 동 까지 걸리는 시간을
     * 조회하여 추가 점수를 부여하고 RecommendedDong.score에 가산하여 리턴합니다
     * @param workplaceDongCode
     * @param recommendedDongList
     * @return 통근거리를 기준으로 점수가 추가된 addCommuteScoreDongList 리스트 반환
     */
    public List<RecommendedDong> calculate(Integer workplaceDongCode, List<RecommendedDong> recommendedDongList){
        List<RecommendedDong> addCommuteScoreDongList= new ArrayList<>();
        for(RecommendedDong rd: recommendedDongList){
            BigDecimal newScore = rd.getScore().add( //Decimal끼리는 + 연산 말고 add 메소드 사용함
                    convertMinutesToScore(
                            getCommuteMinutesByOdsay(workplaceDongCode,rd.getDongCode())));
            rd.setScore(newScore); //점수를 기존 RecommendedDong에 반영
            addCommuteScoreDongList.add(rd);
        }
        return addCommuteScoreDongList;
    }

    /**
     * 오디세이 API를 호출하여 좌표간 통근시간(분)을 반환합니다
     * https://lab.odsay.com/guide/console#searchPubTransPathT
     * @param originDongCode
     * @param destinationDongCode
     * @return 통근시간(분)
     */
    public int getCommuteMinutesByOdsay(Integer originDongCode,Integer destinationDongCode){

        //유효성 검증
        validateDongCode(originDongCode);
        validateDongCode(destinationDongCode);

        Coordinate orginCoor = locationUtil.dongCodeToCoordinate(originDongCode);
        Coordinate destinationCoor = locationUtil.dongCodeToCoordinate(destinationDongCode);

        //TODO: 운영환경에서는 서버 공인아이피로 변경후, 하드코딩 대신 env 파일로 관리
        //NOTE: 여기 있는 키는 현재 김준혁 집 아이피에서만 허용되는 키임, 교육장에서 헷을때 당황하지 않도록
        //NOTE: 오디세이는 api 키에 특수문자가 포함되어있어, 서버가 특수문자를 명령어로 잘못 해석하기 때문에 URLEncoder.encode를 하면 특수문자를 %XX 형식으로 변환
        String encodedApiKey = URLEncoder.encode("OhwYC2oPpPkLspemmNUMro0VB2T3/Eu0KDgYe8ne0zo", StandardCharsets.UTF_8);

        //요청 uri 정의
        String uriString = "https://api.odsay.com/v1/api/searchPubTransPathT"
                + "?apiKey=" + encodedApiKey
                + "&lang=0"
                + "&SX=" + orginCoor.getLongitude()
                + "&SY=" + orginCoor.getLatitude()
                + "&EX=" + destinationCoor.getLongitude()
                + "&EY=" + destinationCoor.getLatitude()
                + "&OPT=0"
                + "&SearchType=0"
                + "&SearchPathType=0";

        //RestClient 로 API 호출
        RestClient restClient = RestClient.create();
        String response=null;
        try{
            response = restClient.get()
                .uri(URI.create(uriString))
                .retrieve()//http 요청을 실행
                .body(String.class);//응답 body를 String 으로 변환

        }catch (Exception e){
            throw new CustomException(ODSAY_API_ERROR);
        }

        return convertOdsayResponseToTotalMinutes(response);
    }

    //오디세이 응답 JSON 값에서 가장 빠른 'totalTime' 을 찾습니다
    //오디세이는 기본적으로 빠른순으로 응답하므로 이를 이용해 get(0) 으로 찾았습니다
    private int convertOdsayResponseToTotalMinutes (String response){
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root
                    .path("result")
                    .path("path")
                    .get(0)
                    .path("info")
                    .path("totalTime")
                    .asInt();
        } catch (JsonProcessingException e) {
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
        if (minutes <= 10) return BigDecimal.valueOf(100);
        if (minutes <= 20) return BigDecimal.valueOf(90);
        if (minutes <= 30) return BigDecimal.valueOf(80);
        if (minutes <= 45) return BigDecimal.valueOf(60);
        if (minutes <= 60) return BigDecimal.valueOf(40);
        if (minutes <= 90) return BigDecimal.valueOf(20);
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
