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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // 순위 없이 동 + 점수만 반환
    public List<RecommendedDong> calculateTopDongs(
            //설문조사 목록
            Survey survey,
            //후보 법정동 코드 목록
            List<Integer> filteredDongCodes
    ) {
        //HIGH/MIDDLE/LOW를 숫자 가중치(1.0/0.5/0.0)로 변환
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

        //중간 계산 저장용 리스트. Row에는 동 1개에 대한 count/density 묶음이 들어감
        List<Row> rows = new ArrayList<>();
        // 1) 동별 count + density(밀도) 계산
        for (DongLocation dong : candidates) {
            //면적이 null, 0이면 1.0으로(0나누기 에러 방지)
            double area = (dong.getDongArea() == null || dong.getDongArea() <= 0.0) ? 1.0 : dong.getDongArea();

            //GROUP BY 결과에 없는 동코드는 기본값 0
            long subwayCount = subwayCountMap.getOrDefault(dong.getDongCode(), 0L);
            long hospitalCount = hospitalCountMap.getOrDefault(dong.getDongCode(), 0L);
            long libraryCount = libraryCountMap.getOrDefault(dong.getDongCode(), 0L);
            long largeStoreCount = largeStoreCountMap.getOrDefault(dong.getDongCode(), 0L);

            //밀도 계산(개수/면적)
            rows.add(new Row(
                    dong,
                    subwayCount / area,
                    hospitalCount / area,
                    libraryCount / area,
                    largeStoreCount / area
            ));
        }

        // 2) 후보 동 중에서 인프라별 최대 밀도 구하기 (정규화 기준 값으로 사용)
        double maxSubwayDensity = rows.stream().mapToDouble(r -> r.subwayDensity).max().orElse(0.0);
        double maxHospitalDensity = rows.stream().mapToDouble(r -> r.hospitalDensity).max().orElse(0.0);
        double maxLibraryDensity = rows.stream().mapToDouble(r -> r.libraryDensity).max().orElse(0.0);
        double maxLargeStoreDensity = rows.stream().mapToDouble(r -> r.largeStoreDensity).max().orElse(0.0);

        return rows.stream()
                .map(r -> {
                    //정규화 공식: (현재밀도/최대밀도)*25
                    //인프라마다 규모가 달라도 0~25 범위에서 비교 가능
                    //인프라 점수 합: 100
                    double subwayNorm = normalizeTo25(r.subwayDensity, maxSubwayDensity);
                    double hospitalNorm = normalizeTo25(r.hospitalDensity, maxHospitalDensity);
                    double libraryNorm = normalizeTo25(r.libraryDensity, maxLibraryDensity);
                    double largeStoreNorm = normalizeTo25(r.largeStoreDensity, maxLargeStoreDensity);

                    //최종 합산 점수
                    double score = (subwayNorm * subwayWeight)
                            + (hospitalNorm * hospitalWeight)
                            + (libraryNorm * libraryWeight)
                            + (largeStoreNorm * largeStoreWeight);

                    return RecommendedDong.builder()
                            .ranking(null)
                            .dongCode(r.dong.getDongCode())
                            .dongName(r.dong.getDongName())
                            .latitude(r.dong.getLatitude())
                            .longitude(r.dong.getLongitude())
                            .score(BigDecimal.valueOf(score))
                            .message("infra: "+BigDecimal.valueOf(score))
                            .build();
                })
                .toList();
    }

    private double normalizeTo25(double value, double max) {
        if (max <= 0.0) return 0.0;
        return (value / max) * 25.0;
    }

    //통 조회 결과에서 동 코드에 해당하는 카운트 수 찾기
    private Map<Integer, Long> toCountMap(List<DongCountDTO> list) {
        return list.stream().collect(Collectors.toMap(DongCountDTO::dongCode, DongCountDTO::cnt));
    }

    private static class Row {
        private final DongLocation dong;
        private final double subwayDensity;
        private final double hospitalDensity;
        private final double libraryDensity;
        private final double largeStoreDensity;

        private Row(
                DongLocation dong,
                double subwayDensity,
                double hospitalDensity,
                double libraryDensity,
                double largeStoreDensity
        ) {
            this.dong = dong;
            this.subwayDensity = subwayDensity;
            this.hospitalDensity = hospitalDensity;
            this.libraryDensity = libraryDensity;
            this.largeStoreDensity = largeStoreDensity;
        }
    }
}
