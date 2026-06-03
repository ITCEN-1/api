package com.itset.itcenteamproject.domain.dashboard.service;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongCountDTO;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.domain.infra.repository.HospitalRepository;
import com.itset.itcenteamproject.domain.infra.repository.LargeStoreRepository;
import com.itset.itcenteamproject.domain.infra.repository.LibraryRepository;
import com.itset.itcenteamproject.domain.infra.repository.SubwayRepository;
import com.itset.itcenteamproject.domain.infra.service.PreferenceWeightService;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InfraScoreCalculator {

    private final DongLocationRepository dongLocationRepository;
    private final SubwayRepository subwayRepository;
    private final HospitalRepository hospitalRepository;
    private final LibraryRepository libraryRepository;
    private final LargeStoreRepository largeStoreRepository;
    private final PreferenceWeightService preferenceWeightService;
    private final RankingMinMaxNormalizer rankingMinMaxNormalizer;

    // 순위 없이 동 + 점수만 반환
    public List<RecommendedDong> calculateTopDongs(
            //설문조사 목록
            Survey survey,
            //후보 법정동 코드 목록
            List<Integer> filteredDongCodes
    ) {
        //HIGH/MIDDLE/LOW를 숫자 가중치(1.0/0.66/0.33)로 변환
        double subwayWeight = preferenceWeightService.toWeight(survey.getPreferenceSubway());
        double hospitalWeight = preferenceWeightService.toWeight(survey.getPreferenceHospital());
        double libraryWeight = preferenceWeightService.toWeight(survey.getPreferenceLibrary());
        double largeStoreWeight = preferenceWeightService.toWeight(survey.getPreferenceLargeStore());

        //후보 동 코드들에 해당하는 동 정보(이름, 좌표, 면적) 조회
        List<DongLocation> candidates = dongLocationRepository.findByDongCodeIn(filteredDongCodes);

        //인프라별 count를 한 번에 집계
        Map<Integer, Long> subwayCountMap = toCountMap(subwayRepository.countByDongCodeIn(filteredDongCodes));
        Map<Integer, Long> hospitalCountMap = toCountMap(hospitalRepository.countByDongCodeIn(filteredDongCodes));
        Map<Integer, Long> libraryCountMap = toCountMap(libraryRepository.countByDongCodeIn(filteredDongCodes));
        Map<Integer, Long> largeStoreCountMap = toCountMap(largeStoreRepository.countByDongCodeIn(filteredDongCodes));

        int subwayMaxCnt = maxInfraCnt(subwayCountMap);
        int hospitalMaxCnt = maxInfraCnt(hospitalCountMap);
        int libraryMaxCnt = maxInfraCnt(libraryCountMap);
        int largeStoreMaxCnt = maxInfraCnt(largeStoreCountMap);

        //중간 계산 저장용 리스트. Row에는 동 1개에 대한 count/density 묶음이 들어감
        PriorityQueue<Row> rowPq = new PriorityQueue<>(Comparator.comparing((Row r) -> r.score).reversed()); //점수 높은 순 정렬
        // 1) 동별 count / 최대 count 계산 및 설문 기반 가중치 적용
        for (DongLocation dong : candidates) {

            //GROUP BY 결과에 없는 동코드는 기본값 0
            long subwayCount = subwayCountMap.getOrDefault(dong.getDongCode(), 0L);
            long hospitalCount = hospitalCountMap.getOrDefault(dong.getDongCode(), 0L);
            long libraryCount = libraryCountMap.getOrDefault(dong.getDongCode(), 0L);
            long largeStoreCount = largeStoreCountMap.getOrDefault(dong.getDongCode(), 0L);

            double subwayScore = (subwayCount / (double) subwayMaxCnt) * 25 * subwayWeight;
            double hospitalScore = (hospitalCount / (double) hospitalMaxCnt) * 25 * hospitalWeight;
            double libraryScore = (libraryCount / (double) libraryMaxCnt) * 25 * libraryWeight;
            double largeStoreScore = (largeStoreCount / (double) largeStoreMaxCnt) * 25 * largeStoreWeight;

            // 인프라 점수 계산
            rowPq.add(new Row(
                dong,
                subwayScore + hospitalScore + libraryScore + largeStoreScore
            ));
        }

        List<RecommendedDong> rankedDongs = new ArrayList<>();
        int rank = 1;
        while(!rowPq.isEmpty()) {
            Row row = rowPq.poll();
            rankedDongs.add(
                    RecommendedDong.builder()
                            .ranking(null)
                            .dongCode(row.dong.getDongCode())
                            .districtName(row.dong.getDistrictName())
                            .dongName(row.dong.getDongName())
                            .latitude(row.dong.getLatitude())
                            .longitude(row.dong.getLongitude())
                            .score(rankingMinMaxNormalizer.getMinMaxNormalizedScore(rank, BigDecimal.valueOf(0), rowPq.size()))
                            .message("infra: "+BigDecimal.valueOf(row.score))
                            .build()
            );
            rank++;
        }

        return rankedDongs;
    }

    //통 조회 결과에서 동 코드에 해당하는 카운트 수 찾기
    private Map<Integer, Long> toCountMap(List<DongCountDTO> list) {
        return list.stream().collect(Collectors.toMap(DongCountDTO::dongCode, DongCountDTO::cnt));
    }

    private static class Row {
        private final DongLocation dong;
        private final double score;
        private Row(
                DongLocation dong,
                double score
        ) {
            this.dong = dong;
            this.score = score;
        }
    }

    private int maxInfraCnt(Map<Integer, Long> infraCountMap) {
        return infraCountMap.values().stream().mapToInt(Long::intValue).max().orElse(0);
    }
}
