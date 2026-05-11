package com.itset.itcenteamproject.domain.infra.service;

import com.itset.itcenteamproject.domain.infra.dto.RecommendedDongDTO;
import com.itset.itcenteamproject.domain.infra.dto.RecommendedDongResultDTO;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.domain.infra.repository.HospitalRepository;
import com.itset.itcenteamproject.domain.infra.repository.LargeStoreRepository;
import com.itset.itcenteamproject.domain.infra.repository.LibraryRepository;
import com.itset.itcenteamproject.domain.infra.repository.SubwayRepository;
import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InfraScoreService {

    private final DongLocationRepository dongLocationRepository;
    private final SubwayRepository subwayRepository;
    private final HospitalRepository hospitalRepository;
    private final LibraryRepository libraryRepository;
    private final LargeStoreRepository largeStoreRepository;
    private final PreferenceWeightService preferenceWeightService;

    //추천 동 리스트
    public RecommendedDongResultDTO calculateTopDongs(
            //후보 동 코드 목록
            List<Integer> filteredDongCodes,
            //설문 선호도(enum)
            PreferenceLevel preferenceSubway,
            PreferenceLevel preferenceHospital,
            PreferenceLevel preferenceLibrary,
            PreferenceLevel preferenceLargeStore,
            //상위 몇 개 뽑을지
            int topN
    ) {
        //HIGH/MIDDLE/LOW를 숫자 가중치(1.0/0.5/0.0)로 변환
        double subwayWeight = preferenceWeightService.toWeight(preferenceSubway);
        double hospitalWeight = preferenceWeightService.toWeight(preferenceHospital);
        double libraryWeight = preferenceWeightService.toWeight(preferenceLibrary);
        double largeStoreWeight = preferenceWeightService.toWeight(preferenceLargeStore);

        //후보 동 코드들에 해당하는 동 정보(이름, 좌표, 면적) 조회
        List<DongLocation> candidates = dongLocationRepository.findByDongCodeIn(filteredDongCodes);

        //중간 계산 저장용 리스트. Row에는 동 1개에 대한 count/density 묶음이 들어감
        List<Row> rows = new ArrayList<>();

        // 1) 동별 count + density(밀도) 계산
        for (DongLocation dong : candidates) {
            //면적이 null, 0이면 1.0으로(0나누기 에러 방지)
            double area = (dong.getDongArea() == null || dong.getDongArea() <= 0.0) ? 1.0 : dong.getDongArea();

            //해당 동의 인프라 실제 개수 조회
            long subwayCount = subwayRepository.countByDongCode(dong.getDongCode());
            long hospitalCount = hospitalRepository.countByDongCode(dong.getDongCode());
            long libraryCount = libraryRepository.countByDongCode(dong.getDongCode());
            long largeStoreCount = largeStoreRepository.countByDongCode(dong.getDongCode());

            //밀도 계산(개수/면적)
            double subwayDensity = subwayCount / area;
            double hospitalDensity = hospitalCount / area;
            double libraryDensity = libraryCount / area;
            double largeStoreDensity = largeStoreCount / area;

            //중간 저장
            rows.add(new Row(
                    dong,
                    subwayCount, hospitalCount, libraryCount, largeStoreCount,
                    subwayDensity, hospitalDensity, libraryDensity, largeStoreDensity
            ));
        }

        // 2) 후보 동 중에서 인프라별 최대 밀도 구하기 (정규화 기준 값으로 사용)
        //orElse(0.0): 비어있을 때 기본값
        double maxSubwayDensity = rows.stream().mapToDouble(r -> r.subwayDensity).max().orElse(0.0);
        double maxHospitalDensity = rows.stream().mapToDouble(r -> r.hospitalDensity).max().orElse(0.0);
        double maxLibraryDensity = rows.stream().mapToDouble(r -> r.libraryDensity).max().orElse(0.0);
        double maxLargeStoreDensity = rows.stream().mapToDouble(r -> r.largeStoreDensity).max().orElse(0.0);

        // 3) 0~100 정규화 후 가중치 적용
        List<RecommendedDongDTO> scored = new ArrayList<>();
        //각 동에 대해 정규화, 최종점수 계산
        for (Row r : rows) {
            //정규화 공식: (현재밀도/최대밀도)*100
            //인프라마다 규모가 달라도 0~100 범위에서 비교 가능
            double subwayNorm = normalizeTo100(r.subwayDensity, maxSubwayDensity);
            double hospitalNorm = normalizeTo100(r.hospitalDensity, maxHospitalDensity);
            double libraryNorm = normalizeTo100(r.libraryDensity, maxLibraryDensity);
            double largeStoreNorm = normalizeTo100(r.largeStoreDensity, maxLargeStoreDensity);

            //최종 합산 점수
            double score = (subwayNorm * subwayWeight)
                    + (hospitalNorm * hospitalWeight)
                    + (libraryNorm * libraryWeight)
                    + (largeStoreNorm * largeStoreWeight);
            //응답용 DTO, 점수뿐 아니라 count, density도 같이 넣어 프론트에서 상세 표시 가능
            scored.add(
                    RecommendedDongDTO.builder()
                            .ranking(0)
                            .dongCode(r.dong.getDongCode())
                            .dongName(r.dong.getDongName())
                            .latitude(r.dong.getLatitude())
                            .longitude(r.dong.getLongitude())
                            .score(score)
                            .message("밀도 정규화(0~100) + 선호도 가중치 반영")
                            //해당 동의 인프라 실제 개수
                            .subwayCount(r.subwayCount)
                            .hospitalCount(r.hospitalCount)
                            .libraryCount(r.libraryCount)
                            .largeStoreCount(r.largeStoreCount)
                            //밀도 별 인프라 갸수
                            .subwayDensity(r.subwayDensity)
                            .hospitalDensity(r.hospitalDensity)
                            .libraryDensity(r.libraryDensity)
                            .largeStoreDensity(r.largeStoreDensity)
                            .build()
            );
        }

        // 4) 점수 내림차순 정렬 + topN
        List<RecommendedDongDTO> top = scored.stream()
                .sorted(Comparator.comparing(RecommendedDongDTO::getScore).reversed())
                .limit(topN)
                .toList();

        // 5) 순위 부여
        List<RecommendedDongDTO> ranked = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            RecommendedDongDTO d = top.get(i);
            ranked.add(
                    RecommendedDongDTO.builder()
                            .ranking(i + 1)
                            .dongCode(d.getDongCode())
                            .dongName(d.getDongName())
                            .latitude(d.getLatitude())
                            .longitude(d.getLongitude())
                            .score(d.getScore())
                            .message(d.getMessage())
                            .subwayCount(d.getSubwayCount())
                            .hospitalCount(d.getHospitalCount())
                            .libraryCount(d.getLibraryCount())
                            .largeStoreCount(d.getLargeStoreCount())
                            .subwayDensity(d.getSubwayDensity())
                            .hospitalDensity(d.getHospitalDensity())
                            .libraryDensity(d.getLibraryDensity())
                            .largeStoreDensity(d.getLargeStoreDensity())
                            .build()
            );
        }

        return RecommendedDongResultDTO.builder()
                .dongs(ranked)
                .build();
    }

    //정규화 (현재밀도/최대밀도)*100
    private double normalizeTo100(double value, double max) {
        if (max <= 0.0) return 0.0; // 전체가 0개인 경우
        return (value / max) * 100.0;
    }

    private static class Row {
        private final DongLocation dong;
        private final long subwayCount;
        private final long hospitalCount;
        private final long libraryCount;
        private final long largeStoreCount;
        private final double subwayDensity;
        private final double hospitalDensity;
        private final double libraryDensity;
        private final double largeStoreDensity;

        private Row(
                DongLocation dong,
                long subwayCount,
                long hospitalCount,
                long libraryCount,
                long largeStoreCount,
                double subwayDensity,
                double hospitalDensity,
                double libraryDensity,
                double largeStoreDensity
        ) {
            this.dong = dong;
            this.subwayCount = subwayCount;
            this.hospitalCount = hospitalCount;
            this.libraryCount = libraryCount;
            this.largeStoreCount = largeStoreCount;
            this.subwayDensity = subwayDensity;
            this.hospitalDensity = hospitalDensity;
            this.libraryDensity = libraryDensity;
            this.largeStoreDensity = largeStoreDensity;
        }
    }
}
