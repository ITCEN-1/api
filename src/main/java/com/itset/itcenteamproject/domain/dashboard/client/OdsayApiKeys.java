package com.itset.itcenteamproject.domain.dashboard.client;

import com.itset.itcenteamproject.exception.CustomException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.itset.itcenteamproject.exception.ErrorCode.ODSAY_API_ERROR;

/**
 * ODsay API 키를 블로킹 큐 기반의 임대 풀로 관리한다.
 * 동일 키를 너무 빠르게 재사용하면 429 가 발생하므로,
 * 키는 사용을 시작(풀에서 take)한 시점부터 COOLDOWN_MS 가 지나야 풀에 반납되어 재사용된다.
 * (응답이 그보다 일찍 와도 반납은 사용 시작 + COOLDOWN_MS 까지 미뤄지고,
 *  요청이 COOLDOWN_MS 보다 오래 걸리면 응답 직후 즉시 반납된다.)
 */
@Slf4j
@Component
public class OdsayApiKeys {

    // 키 사용 시작(take) 이후 풀에 반납되기까지의 최소 점유 시간
    private static final long COOLDOWN_MS = 200L;

    private final BlockingQueue<String> available;
    private final int keyCount;

    // 로그용: 키 → 식별자(#0, #1, ...). 키 원문은 노출하지 않고 인덱스/마스킹으로만 출력한다.
    private final Map<String, String> keyLabels;

    // 쿨다운 후 키를 풀에 반납하는 전용 스레드 (워커 스레드를 100ms 잡아두지 않기 위함)
    private final ScheduledExecutorService cooldownScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "odsay-key-cooldown");
                t.setDaemon(true);
                return t;
            });

    // NOTE: 오디세이는 api 키에 특수문자가 포함되어있어, 서버가 특수문자를 명령어로 잘못 해석하기 때문에 URLEncoder.encode 하여 특수문자를 %XX 형식으로
    public OdsayApiKeys(@Value("${odsay.rest-api-keys}") String[] apiKeys) {
        List<String> encoded = Stream.of(apiKeys)
                .map(key -> URLEncoder.encode(key, StandardCharsets.UTF_8))
                .toList();
        this.keyCount = encoded.size();
        this.available = new LinkedBlockingQueue<>(encoded);

        // 키마다 "#index(앞6자…)" 라벨을 만들어 로그에서 어떤 키가 쓰였는지 식별한다 (원문 미노출)
        Map<String, String> labels = new HashMap<>();
        for (int i = 0; i < encoded.size(); i++) {
            String key = encoded.get(i);
            String prefix = key.length() <= 6 ? key : key.substring(0, 6);
            labels.put(key, "#" + i + "(" + prefix + "…)");
        }
        this.keyLabels = labels;
    }

    public int keyCount() {
        return keyCount;
    }

    /**
     * 사용 가능한 키 하나를 빌려 요청을 실행하고, 사용을 시작한 시점부터 COOLDOWN_MS 가 지나면 풀에 반납한다.
     * 사용 가능한 키가 없으면 take() 에서 블로킹하므로 요청은 FCFS 로 처리된다.
     * @param request 빌린 api 키를 받아 ODsay 요청을 수행하는 함수
     * @return request 의 실행 결과
     */
    public <T> T executeWithKey(Function<String, T> request) {
        String key;
        try {
            key = available.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ODSAY_API_ERROR);
        }

        String label = keyLabels.get(key);
        long start = System.currentTimeMillis();
        log.debug("[ODsay] 키 {} 사용 시작 (thread={})", label, Thread.currentThread().getName());

        try {
            return request.apply(key);
        } finally {
            // 예외가 나도 키는 반드시 반납되어야 풀 고갈을 막을 수 있다.
            // 반납 시점은 "사용 시작 + COOLDOWN_MS" 기준이므로, 이미 그만큼 지났으면(=요청이 오래 걸렸으면) 즉시 반납한다.
            long elapsed = System.currentTimeMillis() - start;
            long delay = Math.max(0L, COOLDOWN_MS - elapsed);
            log.debug("[ODsay] 키 {} 응답 수신 (소요 {}ms), {}ms 후 풀 반납 예정 (사용 시작 + {}ms 기준)",
                    label, elapsed, delay, COOLDOWN_MS);
            cooldownScheduler.schedule(() -> {
                available.offer(key);
                log.debug("[ODsay] 키 {} 쿨다운 종료, 풀 반납 완료", label);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        cooldownScheduler.shutdownNow();
    }
}
