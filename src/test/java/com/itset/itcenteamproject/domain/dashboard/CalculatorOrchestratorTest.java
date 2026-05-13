package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.dashboard.service.CalculatorOrchestrator;
import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Transactional // Spring Test 프레임워크는 트랜잭션을 커밋하지 않고 강제 롤백
public class CalculatorOrchestratorTest {
    @Autowired
    private CalculatorOrchestrator calculatorOrchestrator;
    @Autowired
    private SurveyRepository surveyRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("CalculatorOrchestrator 통합 테스트")
    void calculatorOrchestratorTest1(){
        //given
        User user1 = User.builder()
                .loginId("abc123")
                .password("123456")
                .nickname("abc")
                .build();

        userRepository.save(user1);

        Survey survey1 = Survey.builder()
                .user(user1)
                .workplaceAddress("경기 과천시 과천대로12길 117")
                .jeonseMin(1000)
                .jeonseMax(5000)
                .monthlyMin(null)
                .monthlyMax(null)
                .depositMin(null)
                .depositMax(null)
                .preferenceLargeStore(PreferenceLevel.MIDDLE)
                .preferenceHospital(PreferenceLevel.LOW)
                .preferenceSubway(PreferenceLevel.MIDDLE)
                .preferenceLibrary(PreferenceLevel.LOW)
                .build();

        surveyRepository.save(survey1);

        survey1.addDistricts(List.of("마포구", "송파구", "종로구"));

        //when
        List<RecommendedDong> list = calculatorOrchestrator.getRankingListFromCurrentSurvey(user1.getId());

        //then
        list.forEach(System.out::println);
    }
}
