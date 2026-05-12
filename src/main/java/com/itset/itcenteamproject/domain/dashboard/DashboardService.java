package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.history.History;
import com.itset.itcenteamproject.domain.history.HistoryItem;
import com.itset.itcenteamproject.domain.history.HistoryRepository;
import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import com.itset.itcenteamproject.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_SURVEY;
import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CalculatorOrchestrator calculatorOrchestrator;
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    @Transactional
    public List<RecommendedDong> getRanking(Long userId){

        //1. 설문 바탕으로 점수 산정
        List<RecommendedDong> recommendedDongList =  calculatorOrchestrator.getRankingListFromCurrentSurvey(userId);

        //2. 설문 내용과 응답을 히스토리에 저장
        // 유저 Id 정보에서 설문 정보 가져오기
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(NOT_FOUND_USER));
        Survey survey = surveyRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElseThrow(()-> new CustomException(NOT_FOUND_SURVEY));

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
        for(RecommendedDong rd:recommendedDongList){
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
}
