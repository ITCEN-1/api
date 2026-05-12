package com.itset.itcenteamproject.domain.history;

import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyDTO;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryService {
    private final HistoryRepository historyRepository;
    private final SurveyRepository surveyRepository;

    public HistoryService(HistoryRepository historyRepository, SurveyRepository surveyRepository) {
        this.historyRepository = historyRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * NOTE: userId에 해당하는 Survey와 추천된 동(History)를 불러와 반환한다.
     */
    public List<HistoryDTO> getHistory(String uid, Pageable pageable) {
        List<HistoryDTO> result = new ArrayList<>();
        Long userId = Long.parseLong(uid);
        // userID에 해당하는 Survey를 Page크기만큼 가져오기
        Page<Survey> surveyPage = surveyRepository.findSurveyByUserId(userId, pageable);

        // Survey를 순회하며 SurveyId에 해당하는 History 및 Ranking 데이터를 가져온다.
        for (Survey survey : surveyPage.getContent()) {
            SurveyDTO surveyDto = SurveyDTO.from(survey);
            // surveyID를 바탕으로 history 데이터를 가져온다.
            History history = historyRepository.findHistoriesBySurveyId(survey.getId(), userId);
            // Ranking데이터 가져오기
            List<HistoryItemDTO> rankings = history.getHistoryItems().stream()
                    .map(HistoryItemDTO::from)
                    .toList();
            // 가져온 모든 데이터를 응답 List에 더한다.
            result.add(
                    HistoryDTO.builder()
                            .surveyDto(surveyDto)
                            .rankings(rankings)
                            .build()
            );
        }
        return result;
    }

}
