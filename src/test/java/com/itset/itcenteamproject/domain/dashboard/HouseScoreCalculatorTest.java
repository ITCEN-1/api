package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.HouseContractRepository;
import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import com.itset.itcenteamproject.domain.survey.Survey;
import com.itset.itcenteamproject.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HouseScoreCalculator 테스트")
class HouseScoreCalculatorTest {

    @Mock
    private HouseContractRepository mockRepository;

    private HouseScoreCalculator calculator;
    private Survey testSurvey, wolseTestSurvey;
    private List<RecommendedDong> recommendedDongs;

    @BeforeEach
    void setUp() {
        Map<String, HouseContractRepository> mockMap = new HashMap<>();
        mockMap.put("jeonseRepository", mockRepository);
        mockMap.put("wolseRepository", mockRepository);

        calculator = new HouseScoreCalculator(mockMap);

        // 테스트 사용자
        User testUser = User.builder()
                .loginId("test123")
                .password("password")
                .nickname("테스트")
                .build();

        // 전세 설문 생성
        testSurvey = Survey.builder()
                .workplaceAddress("서울시 강남구")
                .jeonseMin(5000)
                .jeonseMax(25000)
                .monthlyMin(null)
                .monthlyMax(null)
                .depositMin(null)
                .depositMax(null)
                .preferenceLargeStore(PreferenceLevel.HIGH)
                .preferenceHospital(PreferenceLevel.MIDDLE)
                .preferenceSubway(PreferenceLevel.HIGH)
                .preferenceLibrary(PreferenceLevel.LOW)
                .user(testUser)
                .build();

        // 월세 설문 생성
        wolseTestSurvey = Survey.builder()
                .workplaceAddress("서울시 강남구")
                .jeonseMin(null)
                .jeonseMax(null)
                .monthlyMin(50)
                .monthlyMax(150)
                .depositMin(300)
                .depositMax(1500)
                .preferenceLargeStore(PreferenceLevel.HIGH)
                .preferenceHospital(PreferenceLevel.MIDDLE)
                .preferenceSubway(PreferenceLevel.HIGH)
                .preferenceLibrary(PreferenceLevel.LOW)
                .user(testUser)
                .build();

        // 추천 동 리스트
        recommendedDongs = new ArrayList<>();
        recommendedDongs.add(RecommendedDong.builder()
                .ranking(1)
                .dongCode(1001)
                .dongName("강남동")
                .latitude(37.4979)
                .longitude(127.0276)
                .score(BigDecimal.ZERO)
                .build());

        recommendedDongs.add(RecommendedDong.builder()
                .ranking(2)
                .dongCode(1002)
                .dongName("서초동")
                .latitude(37.4845)
                .longitude(127.0365)
                .score(BigDecimal.ZERO)
                .build());

        recommendedDongs.add(RecommendedDong.builder()
                .ranking(3)
                .dongCode(1003)
                .dongName("가락동")
                .latitude(37.4965)
                .longitude(127.1098)
                .score(BigDecimal.ZERO)
                .build());
    }

    @Test
    @DisplayName("전세 계약에 대한 점수 계산 - 정상 케이스")
    void testCalcHousePriceScoreForJeonse() {
        // given
        List<ContractCntDTO> contractList = new ArrayList<>();
        contractList.add(new ContractCntDTO(1001, 1000));
        contractList.add(new ContractCntDTO(1004, 800));
        contractList.add(new ContractCntDTO(1005, 700));
        contractList.add(new ContractCntDTO(1006, 500));
        contractList.add(new ContractCntDTO(1002, 200));
        contractList.add(new ContractCntDTO(1003, 100));

        when(mockRepository.findContractCntByPreference(testSurvey))
                .thenReturn(contractList);

        // when
        List<RecommendedDong> result = calculator.calcHousePriceScore(testSurvey, recommendedDongs);

        // then
        Assertions.assertThat(result.getFirst().getScore()).isEqualTo(new BigDecimal("100.0"));
        Assertions.assertThat(result.get(1).getScore()).isEqualTo(new BigDecimal("20.0"));
        Assertions.assertThat(result.get(2).getScore()).isEqualTo(new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("월세 설문으로 계약 조회 - 정상 케이스")
    void testCalcHousePriceScoreForWolse() {
        // given
        User testUser = User.builder()
                .loginId("test456")
                .password("password")
                .nickname("월세테스트")
                .build();

        List<ContractCntDTO> contractList = new ArrayList<>();
        contractList.add(new ContractCntDTO(1001, 1000));
        contractList.add(new ContractCntDTO(1004, 800));
        contractList.add(new ContractCntDTO(1005, 700));
        contractList.add(new ContractCntDTO(1006, 500));
        contractList.add(new ContractCntDTO(1002, 300));
        contractList.add(new ContractCntDTO(1003, 100));

        when(mockRepository.findContractCntByPreference(wolseTestSurvey))
                .thenReturn(contractList);

        // when
        List<RecommendedDong> result = calculator.calcHousePriceScore(wolseTestSurvey, recommendedDongs);

        // then
        Assertions.assertThat(result.getFirst().getScore()).isEqualTo(new BigDecimal("100.0"));
        Assertions.assertThat(result.get(1).getScore()).isEqualTo(new BigDecimal("30.0"));
        Assertions.assertThat(result.get(2).getScore()).isEqualTo(new BigDecimal("10.0"));
    }
}