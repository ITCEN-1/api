package com.itset.itcenteamproject.domain.dashboard.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.global.vo.Coordinate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OdsayClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OdsayApiKeys odsayApiKeys;

    /**
     * 오디세이 API를 호출하여 좌표간 통근시간(분)을 반환합니다
     * @param startingCoordinate
     * @param destinationCoordinate
     * @return 통근시간(분)
     */
    public String getCommuteMinutes(Coordinate startingCoordinate, Coordinate destinationCoordinate){

        // 키 풀에서 키를 빌려 요청한다. 응답을 받은 뒤 키는 쿨다운 후 풀에 반납된다(429 방지).
        return odsayApiKeys.executeWithKey(apiKey -> {
            // 요청 uri 정의
            String uriString = "https://api.odsay.com/v1/api/searchPubTransPathT"
                    + "?apiKey=" + apiKey
                    + "&lang=0"
                    + "&SX=" + startingCoordinate.getLongitude()
                    + "&SY=" + startingCoordinate.getLatitude()
                    + "&EX=" + destinationCoordinate.getLongitude()
                    + "&EY=" + destinationCoordinate.getLatitude()
                    + "&OPT=0"
                    + "&SearchType=0"
                    + "&SearchPathType=0";

            try {
                return restClient.get()
                        .uri(URI.create(uriString))
                        .retrieve()//http 요청을 실행
                        .body(String.class);//응답 body를 String 으로 변환
            } catch (RestClientException e) {
                throw new CustomException(ODSAY_API_ERROR);
            }
        });
    }

    // 오디세이 응답 JSON 에서 가장 빠른 경로 소요시간을 파싱
    public int convertOdsayResponseToTotalMinutes(String response) {

        if (response == null || response.isBlank()) {
            log.error("[ODsay] 응답이 비어있음. response: '{}'", response);
            throw new CustomException(ODSAY_API_ERROR);
        }

        try {
            JsonNode root = objectMapper.readTree(response);

            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String errorCode = errorNode.path("code").asText();
                String errorMsg = errorNode.path("msg").asText();

                if ("-98".equals(errorCode)) {
                    log.warn("[ODsay] 출도착지가 700m 이내 입니당 code: {}, msg: {}", errorCode, errorMsg);
                    return 0;
                }

                log.warn("[ODsay] 에러 응답 수신. code: {}, msg: {}", errorCode, errorMsg);
                throw new CustomException(ODSAY_API_ERROR);
            }

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
            log.error("[ODsay] 에러 응답 또는 JSON 응답 실패. response: {}", response, e);
            throw new CustomException(ODSAY_BAD_API_RESPONSE);
        }
    }
}
