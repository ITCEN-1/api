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

import static com.itset.itcenteamproject.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommuteScoreCalculator {

    private final LocationService locationUtil;
    private final OdsayClient odsayClient;
    /**
     * workplaceDongCode(직장,학교 동) 를 기준으로 recommendedDongList(추천된 동) 에 있는 각 동 까지 걸리는 시간을
     * 조회하여 추가 점수를 부여하고 RecommendedDong.score에 가산하여 리턴합니다
     * @return 통근거리를 기준으로 점수가 추가된 addCommuteScoreDongList 리스트 반환
     */
    public List<RecommendedDong> calculate(Coordinate workplaceCoordinate, List<RecommendedDong> recommendedDongList){
        // 1) 각 동에 대해 Odsay에서 통근시간을 조회하고, 해당 통근시간으로 점수(convertMinutesToScore) 산정
        //    단, 기존 RecommendedDong.score는 변경하지 않고 commuteTime 필드만 채웁니다.
        // 2) 산정된 통근 점수 기준으로 내림차순 정렬하여 1등~10등까지 ranking을 부여합니다.

        // helper entry to keep computed commute score
        class Entry {
            RecommendedDong rd;
            int minutes;
            BigDecimal commuteScore;
            Entry(RecommendedDong rd, int minutes, BigDecimal commuteScore){
                this.rd = rd;
                this.minutes = minutes;
                this.commuteScore = commuteScore;
            }
        }

        List<Entry> entries = new ArrayList<>();
        for(RecommendedDong rd: recommendedDongList){
            Integer startingDongCode = rd.getDongCode();
            validateDongCode(startingDongCode);
            Coordinate startingDongCoordinate = locationUtil.dongCodeToCoordinate(startingDongCode);

            String odsayResponse = odsayClient.getCommuteMinutes(startingDongCoordinate, workplaceCoordinate);
            int commuteMinutes = odsayClient.convertOdsayResponseToTotalMinutes(odsayResponse);

            BigDecimal commuteScore = convertMinutesToScore(commuteMinutes);

            // update commuteTime in the existing object (preserve original score)
            rd.setCommuteTime(commuteMinutes);

            entries.add(new Entry(rd, commuteMinutes, commuteScore));
        }

        // 정렬: commuteScore 기준 내림차순(높은 점수가 우수), 동률일 경우 rd.score 기준 내림차순
        entries.sort(Comparator.comparing((Entry e) -> e.commuteScore, Comparator.reverseOrder())
                .thenComparing(e -> e.rd.getScore(), Comparator.nullsLast(Comparator.reverseOrder())));

        // 1등부터 10등까지 ranking 부여
        for (int i = 0; i < entries.size(); i++) {
            RecommendedDong rd = entries.get(i).rd;
            if (i < 10) {
                rd.setRanking(i + 1);
            } else {
                rd.setRanking(null);
            }
            // optionally annotate message with commute score
            rd.setMessage((rd.getMessage() == null ? "" : rd.getMessage() + " ") + "commuteScore:" + entries.get(i).commuteScore);
        }
        System.out.println(entries);
        // 반환은 원래 리스트의 순서를 유지하거나, 랭킹이 높은 순으로 반환하고 싶다면 아래처럼 반환
        // 여기서는 ranking이 반영된 추천 리스트를 commuteScore 내림차순으로 반환합니다.
        List<RecommendedDong> result = new ArrayList<>();
        for (Entry e : entries) {
            result.add(e.rd);
            System.out.println("법정동명: " + e.rd.getDongName() + " " + e.rd.getMessage() + " commuteTime:" + e.minutes);
        }
        return result;
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
