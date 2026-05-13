package com.itset.itcenteamproject.domain.dashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
public class OdsayApiKeys {
    private final List<String> apiKeys;
    private final AtomicInteger counter = new AtomicInteger(0);

    // NOTE: 오디세이는 api 키에 특수문자가 포함되어있어, 서버가 특수문자를 명령어로 잘못 해석하기 때문에 URLEncoder.encode 하여 특수문자를 %XX 형식으로
    public OdsayApiKeys(@Value("${odsay.rest-api-keys}") String[] apiKeys) {
        this.apiKeys = Stream.of(apiKeys)
                .map(key -> URLEncoder.encode(key, StandardCharsets.UTF_8))
                .toList();
    }

    public String getNextKey() {
        return apiKeys.get(counter.getAndIncrement() % apiKeys.size());
    }
}
