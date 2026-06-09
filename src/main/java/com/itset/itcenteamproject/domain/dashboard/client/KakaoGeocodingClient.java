package com.itset.itcenteamproject.domain.dashboard.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itset.itcenteamproject.global.vo.Coordinate;
import com.itset.itcenteamproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

/**
 *  https://developers.kakao.com/docs/ko/local/dev-guide#address-coord 다음 공식문서를 참고하였습니다
 */
@Component
@RequiredArgsConstructor
public class KakaoGeocodingClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${kakao.rest-api-key}")// <-- 롬북 Value랑 헷갈려서 20분 버림, 이거 읽는 사람은 그러지마세요
    private String kakaoRestApiKey;

    private static final String KAKAO_ADDRESS_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    // 주소를 좌표로 변환
    public Coordinate addressToCoordinate(String address) {
        String response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> URI.create(
                            KAKAO_ADDRESS_SEARCH_URL + "?query=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                    ))
                    .header("Authorization", "KakaoAK " + kakaoRestApiKey)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new CustomException(KAKAO_API_ERROR);
        }

        return parseCoordinate(response);
    }

    // 카카오 API 응답 내용을 뒤져서 주소로 파싱
    private Coordinate parseCoordinate(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode documents = root.path("documents");

            // 파싱은 됐지만 결과가 없음 → 주소 자체가 잘못된 것
            if (documents.isEmpty()) {
                throw new CustomException(INVALID_WORKPLACE_ADDRESS);
            }

            JsonNode first = documents.get(0);
            Double longitude = first.path("x").asDouble(); // x = longitude (공식문서 참고)
            Double latitude  = first.path("y").asDouble(); // y = latitude (공식문서 참고)

            return new Coordinate(longitude, latitude);
        } catch (JsonProcessingException e) {
            throw new CustomException(KAKAO_API_PARSE_ERROR);
        }
    }
}