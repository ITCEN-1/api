package com.itset.itcenteamproject.domain.dashboard.client;

import com.itset.itcenteamproject.exception.CustomException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.itset.itcenteamproject.exception.ErrorCode.ODSAY_API_ERROR;

/**
 * ODsay API 키를 블로킹 큐로 관리한다
 * 동일 키를 너무 빠르게 재사용하면 429 가 발생하므로,
 * 키는 사용을 시작(풀에서 take)한 시점부터 COOLDOWN_MS 가 지나야 풀에 반납되어 재사용된다.
 */
@Slf4j
@Component
public class OdsayApiKeys {

    // 키 사용 이후 풀에 반납되기까지의 최소 점유 시간
    private static final long COOLDOWN_MS = 200L;

    private final BlockingQueue<String> available;// 키 저장되는 풀
    private final int keyCount; // 이거 기준으로 워커 쓰레드들의 풀이 결정됨

    // 쿨다운 후 키를 풀에 반납하는 전용 스레드 (생산자 소비자 패턴을 반영)
    // 반납까지 워커쓰레드에 위임하면 워커 쓰레드가 노는 상황 발생
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
    }

    public int getKeyCount() {
        return keyCount;
    }

    // 무조건 람다 함수로 받아야함
    public <T> T executeWithKey(Function<String, T> request) {
        String key;
        try {
            key = available.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ODSAY_API_ERROR);
        }

        long start = System.currentTimeMillis();
        log.debug("[ODsay] 키 사용 시작 (thread={})", Thread.currentThread().getName());

        try {
            return request.apply(key);// 받았던 람다 함수를 실제로 실행시키는 부분
        } finally {
            // 예외가 나도 키는 반드시 반납되어야하므로 finally로

            long elapsed = System.currentTimeMillis() - start;//디 버어어어어 그
            long delay = Math.max(0L, COOLDOWN_MS - elapsed);
            log.debug("[ODsay] 응답 수신 (소요 {}ms), {}ms 후 풀 반납 예정 (사용 시작 + {}ms 기준)",
                    elapsed, delay, COOLDOWN_MS);//디 버어어어어 그
            cooldownScheduler.schedule(() -> {
                available.offer(key);
                log.debug("[ODsay] 키 쿨다운 종료, 풀 반납 완료");
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        cooldownScheduler.shutdownNow();
    }
}
