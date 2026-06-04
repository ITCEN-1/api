package com.itset.itcenteamproject.odsayRateLimitTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ODsay "단일 API 키"의 호출 간격 제한(rate limit)을 탐색하는 전용 테스트.
 *
 * 목적: 키 풀/병렬 로직과 무관하게, 하나의 키로 일정한 텀(interval)을 두고 연속 요청했을 때
 *       어느 간격부터 429(Too Many Requests)가 사라지는지(=안전한 최소 호출 간격) 확인한다.
 *
 * 실행:  ./gradlew test --tests "com.itset.itcenteamproject.odsayRateLimitTest.OdsayRateLimitProbeTest"
 *       (application-secret 등 ODSAY_REST_API_KEY_LIST 설정이 있어야 함. 실제 ODsay 서버를 호출하는 LIVE 테스트)
 *
 * 결과는 콘솔에 간격별 통계로 출력된다. 단정(assert)하지 않고 관찰/리포트만 한다.
 */
@SpringBootTest
public class OdsayRateLimitProbeTest {

    @Value("${odsay.rest-api-keys}")
    private String[] rawKeys;

    private final RestClient restClient = RestClient.create();

    // 탐색할 호출 간격(ms). 0(연속) 부터 점점 늘려가며 429가 사라지는 임계점을 찾는다.
    private static final long[] INTERVALS_MS = {0, 30, 50, 80, 100, 150, 200, 300};

    // 각 간격에서 보낼 연속 요청 수 (많을수록 신뢰도↑, 호출 쿼터 소모↑)
    private static final int REQUESTS_PER_INTERVAL = 10;

    // 간격 그룹 사이의 휴식 시간(ms). 이전 그룹의 영향이 다음 그룹에 남지 않도록 충분히 쉰다.
    private static final long RESET_GAP_MS = 5_000;

    // 고정 좌표 (종로3가역 -> 롯데타워). 경로 자체보다 호출 빈도가 관심사이므로 값은 임의.
    private static final double SX = 126.9923298, SY = 37.57038842;
    private static final double EX = 127.103050,  EY = 37.512615;

    @Test
    @DisplayName("단일 ODsay 키로 호출 간격을 바꿔가며 429 발생 임계 간격 탐색")
    void probeSingleKeyRateLimit() throws InterruptedException {
        String apiKey = URLEncoder.encode(rawKeys[0], StandardCharsets.UTF_8);
        System.out.printf("[Probe] 단일 키(#0, %s…)로 rate limit 탐색 시작. 간격당 %d회 요청%n",
                rawKeys[0].substring(0, Math.min(6, rawKeys[0].length())), REQUESTS_PER_INTERVAL);

        for (long interval : INTERVALS_MS) {
            int ok = 0, tooMany = 0, other = 0, firstTooManyAt = -1;

            for (int i = 1; i <= REQUESTS_PER_INTERVAL; i++) {
                Result r = call(apiKey);

                if (r.rateLimited()) {
                    tooMany++;
                    if (firstTooManyAt < 0) firstTooManyAt = i;
                } else if (r.httpStatus() == 200) {
                    ok++;
                } else {
                    other++;
                }

                System.out.printf("  [interval=%dms] req#%-2d -> httpStatus=%d%s%n",
                        interval, i, r.httpStatus(), r.note().isEmpty() ? "" : " (" + r.note() + ")");

                if (i < REQUESTS_PER_INTERVAL) {
                    Thread.sleep(interval);
                }
            }

            System.out.printf(
                    "[Probe][요약] interval=%dms | 정상=%d, 429/제한=%d, 기타=%d | 첫 제한 발생=%s%n%n",
                    interval, ok, tooMany, other,
                    firstTooManyAt < 0 ? "없음" : ("req#" + firstTooManyAt));

            // 다음 간격 그룹으로 넘어가기 전 휴식 (이전 그룹의 폭주 영향 제거)
            Thread.sleep(RESET_GAP_MS);
        }

        System.out.println("[Probe] 완료. 429/제한 발생이 사라지는 가장 작은 interval 이 안전한 최소 호출 간격입니다.");
    }

    /**
     * 한 번 호출하고 HTTP 상태/제한 여부를 반환한다. 4xx/5xx 에도 예외를 던지지 않도록 에러 핸들링을 비활성화한다.
     * ODsay 는 본문에 에러를 담아 200 으로 주는 경우도 있어 본문도 함께 검사한다.
     */
    private Result call(String apiKey) {
        String uri = "https://api.odsay.com/v1/api/searchPubTransPathT"
                + "?apiKey=" + apiKey
                + "&lang=0"
                + "&SX=" + SX + "&SY=" + SY
                + "&EX=" + EX + "&EY=" + EY
                + "&OPT=0&SearchType=0&SearchPathType=0";

        try {
            ResponseEntity<String> resp = restClient.get()
                    .uri(URI.create(uri))
                    .retrieve()
                    .onStatus(status -> true, (req, res) -> { /* 어떤 상태에도 예외 던지지 않음 */ })
                    .toEntity(String.class);

            int status = resp.getStatusCode().value();
            String body = resp.getBody() == null ? "" : resp.getBody();

            // HTTP 429 또는 본문에 limit/too many 류 에러가 담긴 경우를 "제한"으로 본다.
            boolean rateLimited = status == 429
                    || body.toLowerCase().contains("too many")
                    || body.toLowerCase().contains("limit");

            String note = "";
            if (status != 200) {
                note = "body=" + truncate(body);
            } else if (rateLimited) {
                note = "200이지만 본문 제한 응답: " + truncate(body);
            }
            return new Result(status, rateLimited, note);

        } catch (Exception e) {
            // 네트워크 오류 등 (예외도 콘솔에 남겨 원인 파악)
            return new Result(-1, false, "예외:" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }
    }

    private static String truncate(String s) {
        if (s == null) return "";
        return s.length() <= 200 ? s : s.substring(0, 200) + "…";
    }

    private record Result(int httpStatus, boolean rateLimited, String note) {}
}
