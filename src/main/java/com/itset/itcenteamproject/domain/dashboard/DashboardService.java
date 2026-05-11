package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.dto.DongDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraDetailResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraItemResponse;
import com.itset.itcenteamproject.domain.dashboard.dto.InfraType;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.JeonseRepository;
import com.itset.itcenteamproject.domain.house.WolseRepository;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.domain.infra.repository.HospitalRepository;
import com.itset.itcenteamproject.domain.infra.repository.LargeStoreRepository;
import com.itset.itcenteamproject.domain.infra.repository.LibraryRepository;
import com.itset.itcenteamproject.domain.infra.repository.SubwayRepository;
import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyService;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.itset.itcenteamproject.domain.dashboard.dto.InfraType.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DongLocationRepository dongLocationRepository;
    private final HospitalRepository hospitalRepository;
    private final SubwayRepository subwayRepository;
    private final LibraryRepository libraryRepository;
    private final LargeStoreRepository largeStoreRepository;
    private final JeonseRepository jeonseRepository;
    private final WolseRepository wolseRepository;
    private final SurveyService surveyService;

    /**
     * 동 상세 요약 조회
     * - userId: 로그인 사용자 검증용
     * - surveyId: 과거 설문 기준 조회용
     * - dongCode: 상세 조회 대상 동
     * - top10DongCodes: 추천 결과 10개 동 코드
     */
    public DongDetailResponse getDongSummary(
            Long userId,
            Long surveyId,
            Integer dongCode
            )
    {

        //surveyId가 userId의 설문인지 검증 + 설문 조회
        Survey survey = surveyService.findByIdAndUserId(surveyId, userId);

        //동 기본정보 조회
        DongLocation dong = dongLocationRepository.findById(dongCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동 코드입니다."));

        //인프라 개수 조회 (동 기준)
        long hospitalCount = hospitalRepository.countByDongCode(dongCode);
        long subwayCount = subwayRepository.countByDongCode(dongCode);
        long libraryCount = libraryRepository.countByDongCode(dongCode);
        long largeStoreCount = largeStoreRepository.countByDongCode(dongCode);

        //문 기준 전/월세 집계 결과에서 현재 dongCode 개수만 추출
        Map<Integer, Long> jeonseMap = toCountMap(jeonseRepository.findContractCntByPreference(survey));
        Map<Integer, Long> wolseMap = toCountMap(wolseRepository.findContractCntByPreference(survey));

        long jeonseCount = jeonseMap.getOrDefault(dongCode, 0L);
        long wolseCount = wolseMap.getOrDefault(dongCode, 0L);

        //응답 DTO 반환
        return DongDetailResponse.builder()
                .surveyId(surveyId)
                .dongCode(dong.getDongCode())
                .dongName(dong.getDongName())
                .latitude(dong.getLatitude())
                .longitude(dong.getLongitude())
                .hospitalCount(hospitalCount)
                .subwayCount(subwayCount)
                .libraryCount(libraryCount)
                .largeStoreCount(largeStoreCount)
                .jeonseCount(jeonseCount)
                .wolseCount(wolseCount)
                .build();
    }

    /**
     * 인프라 요소별 상세 조회
     * - type에 따라 이름/위도/경도 목록 반환
     */
    public InfraDetailResponse getInfraDetails(
            Long userId,
            Long surveyId,
            Integer dongCode,
            InfraType type
    ) {
        // 1) 설문 권한 검증 (본인 설문인지)
        surveyService.findByIdAndUserId(surveyId, userId);

        // 2) 동 존재 확인
        dongLocationRepository.findById(dongCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        // 3) type별 목록 조회 + 공통 DTO 변환
        List<InfraItemResponse> items = switch (type) {
            case HOSPITAL -> hospitalRepository.findByDongCode(dongCode).stream()
                    .map(h -> InfraItemResponse.builder()
                            .name(h.getName())
                            .latitude(h.getLatitude())
                            .longitude(h.getLongitude())
                            .build())
                    .toList();

            case SUBWAY -> subwayRepository.findByDongCode(dongCode).stream()
                    .map(s -> InfraItemResponse.builder()
                            .name(s.getName())
                            .latitude(s.getLatitude())
                            .longitude(s.getLongitude())
                            .build())
                    .toList();

            case LIBRARY -> libraryRepository.findByDongCode(dongCode).stream()
                    .map(l -> InfraItemResponse.builder()
                            .name(l.getName())
                            .latitude(l.getLatitude())
                            .longitude(l.getLongitude())
                            .build())
                    .toList();

            case LARGE_STORE -> largeStoreRepository.findByDongCode(dongCode).stream()
                    .map(ls -> InfraItemResponse.builder()
                            .name(ls.getName())
                            .latitude(ls.getLatitude())
                            .longitude(ls.getLongitude())
                            .build())
                    .toList();
        };

        // 4) 응답 반환
        return InfraDetailResponse.builder()
                .surveyId(surveyId)
                .dongCode(dongCode)
                .type(type)
                .items(items)
                .build();
    }


    private Map<Integer, Long> toCountMap(List<ContractCntDTO> list) {
        return list.stream().collect(Collectors.toMap(
                ContractCntDTO::getDongCode,
                ContractCntDTO::getCnt,
                (a, b) -> a
        ));
    }
}
