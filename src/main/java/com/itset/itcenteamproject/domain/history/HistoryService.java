package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import com.itset.itcenteamproject.domain.survey.dto.SurveyDTO;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class
HistoryService {
    private final HistoryRepository historyRepository;
    private final SurveyRepository surveyRepository;

    public HistoryService(HistoryRepository historyRepository, SurveyRepository surveyRepository) {
        this.historyRepository = historyRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * NOTE: userId에 해당하는 Survey와 추천된 동(History)를 불러와 반환한다.
     */
    public List<HistoryDTO> getHistory(Long userId, Pageable pageable) {
        List<HistoryDTO> result = new ArrayList<>();
        // userID에 해당하는 Survey를 Page크기만큼 가져오기
        Page<Survey> surveyPage = surveyRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        log.info(surveyPage.getContent().toString());

        // Survey를 순회하며 SurveyId에 해당하는 History 및 Ranking 데이터를 가져온다.
        for (Survey survey : surveyPage.getContent()) {
            SurveyDTO surveyDto = SurveyDTO.from(survey);
            // surveyID를 바탕으로 history 데이터를 가져온다.
            History history = historyRepository.findHistoriesBySurveyId(survey.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.NO_HISTORY_DATA));
            log.info(history.toString());
            // Ranking데이터 가져오기
            List<RecommendedDong> recommendedDongs = history.getHistoryItems().stream()
                    //HistoryItemDTO가 사라져서 변환로직이 사려졌기 때문에 HistoryService 에 만들어둠
                    .map(HistoryService::toRecommendedDong)
                    .toList();
            // 가져온 모든 데이터를 응답 List에 더한다.
            result.add(
                    HistoryDTO.builder()
                            .surveyDto(surveyDto)
                            .recommendedDongs(recommendedDongs)
                            .build()
            );
        }
        return result;
    }

    public HistoryDTO getHistory(Long surveyId) {
        Survey survey = surveyRepository.findSurveyById(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SURVEY));

        History history = historyRepository.findHistoriesBySurveyId(surveyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_HISTORY_DATA));

        return HistoryDTO.builder()
                .surveyDto(SurveyDTO.from(survey))
                //HistoryItemDTO가 사라져서 변환로직이 사려졌기 때문에 HistoryService 에 만들어둠
                .recommendedDongs(history.getHistoryItems().stream()
                        .map(HistoryService::toRecommendedDong)
                        .collect(Collectors.toList()))
                .build();

    }

    // HistoryItemDTO가 사라져서 변환로직이 사라져서 만들었음
    // HistoryItem(저장된 추천 결과) -> RecommendedDong 변환
    // 주의 ** 히스토리 흐름에는 score/message가 저장돼 있지 않으므로 채우지 않는다.
    private static RecommendedDong toRecommendedDong(HistoryItem historyItem) {
        DongLocation dongLocation = historyItem.getDongLocation();

        return RecommendedDong.builder()
                .ranking(historyItem.getRanking())
                .dongCode(historyItem.getDongCode())
                .districtName(dongLocation.getDistrictName())
                .dongName(dongLocation.getDongName())
                .latitude(dongLocation.getLatitude())
                .longitude(dongLocation.getLongitude())
                .commuteTime(historyItem.getCommuteTime())
                .build();
    }

}
