package com.itset.itcenteamproject.domain.dashboard.service;

import com.itset.itcenteamproject.domain.dashboard.client.OdsayClient;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.global.vo.Coordinate;
import com.itset.itcenteamproject.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommuteScoreCalculator {

    private final LocationService locationUtil;
    private final OdsayClient odsayClient;
    private final ExecutorService odsayExecutor;
    private final RankingMinMaxNormalizer rankingMinMaxNormalizer;

    /**
     * workplaceDongCode(직장,학교 동) 를 기준으로 recommendedDongList(추천된 동) 에 있는 각 동 까지 걸리는 시간을
     * 조회하여 통근시간 기반 점수를 산정하고, 내림차순으로 정렬하여 top10에 ranking을 지정합니다
     * @return 통근시간 기반으로 ranking이 설정된 RecommendedDong 리스트 반환
     */
    public List<RecommendedDong> calculate(Coordinate workplaceCoordinate, List<RecommendedDong> recommendedDongList){

        // 1) 동 좌표 변환은 JPA(EntityManager)를 타므로 트랜잭션 스레드에서 먼저 모두 수행한다.
        List<DongWithCoordinate> prepared = new ArrayList<>();
        for (RecommendedDong rd : recommendedDongList) {
            validateDongCode(rd.getDongCode());
            Coordinate startingDongCoordinate = locationUtil.dongCodeToCoordinate(rd.getDongCode());
            prepared.add(new DongWithCoordinate(rd, startingDongCoordinate));
        }

        // 2) 순수 HTTP 호출(ODsay)만 병렬 실행한다.
        List<CompletableFuture<Entry>> futures = new ArrayList<>();
        for(DongWithCoordinate p:prepared){
            CompletableFuture<Entry> completableFuture = CompletableFuture.supplyAsync(
                    () -> buildCommuteEntry(p.recommendedDong(), p.startingCoordinate(), workplaceCoordinate),
                    odsayExecutor
            );
            futures.add(completableFuture);
        }

        // 3) Entry 형태로 결과를 수집하고, 통근시간(분) 기준으로 정렬 후 ranking 할당 및 정규화 점수 가산
        try {
            List<Entry> entries = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // 정렬: commuteMinutes 오름차순(작을수록 우수), 통근분 동일하면 기존 rd.score 내림차순(높을수록 우수)
            entries.sort(Comparator
                    .comparingInt((Entry e) -> convertMinutesToScore(e.commuteMinutes).intValue()).reversed()
                    .thenComparing((Entry e) -> e.rd.getScore(), Comparator.nullsLast(Comparator.reverseOrder()))// commuteScore 기준 내림차순
            );

            int size = entries.size();
            List<RecommendedDong> result = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                RecommendedDong rd = entry.rd;

                int rank = i + 1; // 1-based rank
                BigDecimal normalized = rankingMinMaxNormalizer.getMinMaxNormalizedScore(rank, BigDecimal.valueOf(0), size);
                BigDecimal existing = rd.getScore() != null ? rd.getScore() : BigDecimal.ZERO;

                rd.setScore(existing.add(normalized));
                rd.setRanking(rank);
                rd.setMessage("법정동명: " + rd.getDongName() + " " + rd.getMessage() + " commuteNormalized:" + normalized + " total:" + rd.getScore());

                if (rank <= 10) {
                    rd.setRanking(rank);
                } else {
                    rd.setRanking(null);
                }
                result.add(rd);
            }
            return result.stream().limit(10).toList();

        } catch (CompletionException e) {
            if (e.getCause() instanceof CustomException ce) {
                throw ce;
            }
            throw e;
        }
    }

    // 동 하나에 대해 ODsay 통근시간을 조회하고 Entry로 감싸서 반환한다
    private Entry buildCommuteEntry(RecommendedDong rd, Coordinate startingDongCoordinate, Coordinate workplaceCoordinate) {
        String odsayResponse = odsayClient.getCommuteMinutes(startingDongCoordinate, workplaceCoordinate);
        int commuteMinutes = odsayClient.convertOdsayResponseToTotalMinutes(odsayResponse);

        BigDecimal commuteScore = convertMinutesToScore(commuteMinutes);

        String newMessage = rd.getMessage() + " commute(raw:" + commuteScore + " min:" + commuteMinutes + ")";

        RecommendedDong updatedRd = RecommendedDong.builder()
                .commuteTime(commuteMinutes)
                .dongCode(rd.getDongCode())
                .dongName(rd.getDongName())
                .score(rd.getScore())  // 기존 score 유지
                .longitude(rd.getLongitude())
                .latitude(rd.getLatitude())
                .message(newMessage)
                .build();

        return new Entry(updatedRd, commuteMinutes, commuteScore);
    }

    // 동과 변환된 시작 좌표를 묶는 내부 레코드
    private record DongWithCoordinate(RecommendedDong recommendedDong, Coordinate startingCoordinate) {}

    // 동과 통근 정보를 묶는 내부 클래스
    private static class Entry {
        RecommendedDong rd;
        int commuteMinutes;
        BigDecimal commuteScore;

        Entry(RecommendedDong rd, int commuteMinutes, BigDecimal commuteScore) {
            this.rd = rd;
            this.commuteMinutes = commuteMinutes;
            this.commuteScore = commuteScore;
        }
    }

    /**
     * 통근시간(분)을 점수로 수치화하는 메소드입니다 산정 방식은 임의로 정했습니다
     * @param minutes
     * @return 점수
     */
    private BigDecimal convertMinutesToScore(int minutes) {
        if (minutes <= 10) return BigDecimal.valueOf(50);  // 만족도 100퍼
        if (minutes <= 20) return BigDecimal.valueOf(45);  // 만족도 94퍼
        if (minutes <= 30) return BigDecimal.valueOf(40);  // 만족도 86퍼
        if (minutes <= 42) return BigDecimal.valueOf(35);  // 직장인 희망 통근시간
        if (minutes <= 51) return BigDecimal.valueOf(25);  // 이직,이사 심각 고려 임계점
        if (minutes <= 58) return BigDecimal.valueOf(15);  // 장시간 통근 임계점
        if (minutes <= 90) return BigDecimal.valueOf(5);   // 장시간 통근 구간
        return BigDecimal.valueOf(0);
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
