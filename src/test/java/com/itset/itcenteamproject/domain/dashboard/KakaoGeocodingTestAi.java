package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.infra.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // 실제 스프링 빈을 모두 로드하여 "진짜" 환경을 구축합니다.
@ActiveProfiles("test") // src/test/resources/application-test.yml 등을 사용한다면 설정
class KakaoGeocodingTestAi {

    @Autowired
    private KakaoGeocodingClient kakaoGeocodingClient;

    @Test
    @DisplayName("LIVE: 실제 카카오 API 서버에 요청을 보내 좌표를 가져온다")
    void realApiCallTest() {
        // given: 실제 존재하는 주소들
        String[] addresses = {
                "경기 과천시 과천대로12길 117",
                "경기 성남시 분당구 판교역로 166",
                "경기 성남시 분당구 정자일로 95",
                "서울 송파구 송파대로 570",
                "서울 송파구 위례성대로 2"
        };

        for (String address : addresses) {
            // when: 가짜(Mock)가 아닌 실제 객체의 실제 메서드 호출
            Coordinate result = kakaoGeocodingClient.addressToCoordinate(address);

            // then: 결과 검증 (실제 좌표값이 찍히는지 확인)
            System.out.println("주소: " + address);
            System.out.println("결과: 위도(" + result.getLatitude() + "), 경도(" + result.getLongitude() + ")");

            assertThat(result.getLatitude()).isNotNull();
            assertThat(result.getLongitude()).isNotNull();
            assertThat(result.getLatitude()).isGreaterThan(30.0); // 한국 위도 범위 체크
        }
    }
}