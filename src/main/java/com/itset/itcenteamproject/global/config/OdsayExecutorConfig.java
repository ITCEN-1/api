package com.itset.itcenteamproject.global.config;

import com.itset.itcenteamproject.domain.dashboard.client.OdsayApiKeys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ODsay 통근시간 조회를 병렬로 실행하기 위한 고정 크기 스레드풀.
 * 동시 요청 수는 키 개수만큼이면 충분하므로 풀 크기를 키 개수에 맞춘다.
 * 초과 작업은 풀 큐에서 대기하고, 키 풀의 블로킹으로 실제 동시성이 제한됨
 */
@Configuration
public class OdsayExecutorConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService odsayExecutor(OdsayApiKeys odsayApiKeys) {
        return Executors.newFixedThreadPool(odsayApiKeys.getKeyCount());
    }
}
