package com.itset.itcenteamproject.domain.dashboard.service;

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
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.history.History;
import com.itset.itcenteamproject.domain.history.HistoryItem;
import com.itset.itcenteamproject.domain.history.HistoryRepository;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyService;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_SURVEY;
import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CalculatorOrchestrator calculatorOrchestrator;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final DongLocationRepository dongLocationRepository;
    private final HospitalRepository hospitalRepository;
    private final SubwayRepository subwayRepository;
    private final LibraryRepository libraryRepository;
    private final LargeStoreRepository largeStoreRepository;
    private final JeonseRepository jeonseRepository;
    private final WolseRepository wolseRepository;
    private final SurveyService surveyService;

    @Transactional
    public List<RecommendedDong> getRanking(Long userId) {

        //1. 설문 바탕으로 점수 산정
        List<RecommendedDong> recommendedDongList = calculatorOrchestrator.getRankingListFromCurrentSurvey(userId);

        //2. 설문 내용과 응답을 히스토리에 저장
        // 유저 Id 정보에서 설문 정보 가져오기
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        Survey survey = surveyRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElseThrow(() -> new CustomException(NOT_FOUND_SURVEY));

        // 동일 설문에 대한 히스토리가 이미 존재하면 저장 생략
        if (historyRepository.existsBySurveyId(survey.getId())) {
            return recommendedDongList;
        }

        // 히스토리 생성
        History history = History.builder()
                .survey(survey)
                .user(user)
                .build();

        // 히스토리 객체 영속화
        historyRepository.save(history);

        // 추가된 History의 HistoryItem 을 넣어준다
        for (RecommendedDong rd : recommendedDongList) {
            HistoryItem historyItem = HistoryItem.builder()
                    .history(history)
                    .ranking(rd.getRanking())
                    .dongCode(rd.getDongCode())
                    .commuteTime(rd.getCommuteTime())
                    .build();
            history.getHistoryItems().add(historyItem); // 영속화 되었으므로 더티체킹된다
        }
        return recommendedDongList;
    }

    public DongDetailResponse getDongSummary(
            Long userId,
            Long surveyId,
            Integer dongCode
    ) {
        //surveyId가 userId의 설문인지 검증 + 설문 조회
        Survey survey = surveyService.findByIdAndUserId(surveyId, userId);

        //동 기본정보 조회(동 존재 여부 확인 + 이름, 위/경도표)
        DongLocation dong = dongLocationRepository.findById(dongCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        //인프라 개수 조회 (동 기준)
        long hospitalCount = hospitalRepository.countByDongCode(dongCode);
        long subwayCount = subwayRepository.countByDongCode(dongCode);
        long libraryCount = libraryRepository.countByDongCode(dongCode);
        long largeStoreCount = largeStoreRepository.countByDongCode(dongCode);

        //설문 기준 전/월세 집계 결과에서 현재 dongCode 개수만 추출
        Map<Integer, Long> jeonseMap = toCountMap(jeonseRepository.findContractCntByPreference(survey));
        Map<Integer, Long> wolseMap = toCountMap(wolseRepository.findContractCntByPreference(survey));

        long jeonseCount = jeonseMap.getOrDefault(dongCode, 0L);
        long wolseCount = wolseMap.getOrDefault(dongCode, 0L);

        //설문조사에 있는 동-직장까지 걸리는 시간 메시지
        History history = historyRepository.findHistoriesBySurveyId(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_HISTORY_DATA));

        Integer commuteTime = null;
        String commuteMessage = null;

        if (history != null && history.getHistoryItems() != null) {
            HistoryItem matched = history.getHistoryItems().stream()
                    .filter(item -> dongCode.equals(item.getDongCode()))
                    .findFirst()
                    .orElse(null);

            if (matched != null && matched.getCommuteTime() != null) {
                commuteTime = matched.getCommuteTime();
                commuteMessage = "해당 주소까지 " + commuteTime + "분이 걸립니다";
            }
        }

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
                .commuteTime(commuteTime)
                .commuteMessage(commuteMessage)
                .build();
    }

    //인프라 요소별 상세 조회
    //type에 따라 이름/위도/경도 목록 반환
    public InfraDetailResponse getInfraDetails(
            Integer dongCode,
            InfraType type
    ) {
        // 1) 동 존재 확인
        dongLocationRepository.findById(dongCode)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        // 2) type별 목록 조회 + 공통 DTO 변환
        List<InfraItemResponse> items = switch (type) {
            //해당 동의 병원 엔티티 목록 조회
            case HOSPITAL -> hospitalRepository.findByDongCode(dongCode).stream()
                    //병원 엔티티 하나를 InfraItemResponse로 변환해서 필요한 필드 추출
                    //DB 엔티티를 API응답용 DTO로 매핑
                    .map(h -> InfraItemResponse.builder()
                            .id(h.getId())
                            .name(h.getName())
                            .latitude(h.getLatitude())
                            .longitude(h.getLongitude())
                            .build())
                    .toList();

            case SUBWAY -> subwayRepository.findByDongCode(dongCode).stream()
                    .map(s -> InfraItemResponse.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .latitude(s.getLatitude())
                            .longitude(s.getLongitude())
                            .line(s.getLine().strip())
                            .build())
                    .toList();

            case LIBRARY -> libraryRepository.findByDongCode(dongCode).stream()
                    .map(l -> InfraItemResponse.builder()
                            .id(l.getId())
                            .name(l.getName())
                            .latitude(l.getLatitude())
                            .longitude(l.getLongitude())
                            .build())
                    .toList();

            case LARGE_STORE -> largeStoreRepository.findByDongCode(dongCode).stream()
                    .map(ls -> InfraItemResponse.builder()
                            .id(ls.getId())
                            .name(ls.getName())
                            .latitude(ls.getLatitude())
                            .longitude(ls.getLongitude())
                            .build())
                    .toList();
        };

        // 3) 응답 반환
        return InfraDetailResponse.builder()
                .dongCode(dongCode)
                .type(type)
                .items(items)
                .build();
    }

    //리스트 형태 집계 결과를 동코드 → 개수 map으로 변환
    private Map<Integer, Long> toCountMap(List<ContractCntDTO> list) {
        return list.stream().collect(Collectors.toMap(
                ContractCntDTO::getDongCode, //key
                ContractCntDTO::getCnt, //value
                (a, b) -> a
        ));
    }
}
