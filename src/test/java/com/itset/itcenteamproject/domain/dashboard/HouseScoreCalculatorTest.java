package com.itset.itcenteamproject.domain.dashboard;

import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.dashboard.service.HouseScoreCalculator;
import com.itset.itcenteamproject.domain.dashboard.service.RankingMinMaxNormalizer;
import com.itset.itcenteamproject.domain.house.ContractCntDTO;
import com.itset.itcenteamproject.domain.house.HouseContractRepository;
import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
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
    @Mock
    private RankingMinMaxNormalizer mockNormalizer;

    private HouseScoreCalculator calculator;
    private Survey testSurvey, wolseTestSurvey;
    private List<RecommendedDong> recommendedDongs;

    @BeforeEach
    void setUp() {
        Map<String, HouseContractRepository> mockMap = new HashMap<>();
        mockMap.put("jeonseRepository", mockRepository);
        mockMap.put("wolseRepository", mockRepository);

        calculator = new HouseScoreCalculator(mockMap, mockNormalizer);

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
        contractList.add(new ContractCntDTO(1001, 1000L));
        contractList.add(new ContractCntDTO(1004, 800L));
        contractList.add(new ContractCntDTO(1005, 700L));
        contractList.add(new ContractCntDTO(1006, 500L));
        contractList.add(new ContractCntDTO(1002, 200L));
        contractList.add(new ContractCntDTO(1003, 100L));

        when(mockRepository.findContractCntByPreferenceInDongCodes(testSurvey, contractList.stream().map(ContractCntDTO::getDongCode).toList()))
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
        contractList.add(new ContractCntDTO(1001, 1000L));
        contractList.add(new ContractCntDTO(1004, 800L));
        contractList.add(new ContractCntDTO(1005, 700L));
        contractList.add(new ContractCntDTO(1006, 500L));
        contractList.add(new ContractCntDTO(1002, 300L));
        contractList.add(new ContractCntDTO(1003, 100L));

        when(mockRepository.findContractCntByPreferenceInDongCodes(wolseTestSurvey, contractList.stream().map(ContractCntDTO::getDongCode).toList()))
                .thenReturn(contractList);

        // when
        List<RecommendedDong> result = calculator.calcHousePriceScore(wolseTestSurvey, recommendedDongs);

        // then
        Assertions.assertThat(result.getFirst().getScore()).isEqualTo(new BigDecimal("100.0"));
        Assertions.assertThat(result.get(1).getScore()).isEqualTo(new BigDecimal("30.0"));
        Assertions.assertThat(result.get(2).getScore()).isEqualTo(new BigDecimal("10.0"));
    }

    @Test
    @DisplayName("점수 내림차순으로 top 10 추출 확인")
    void testTop10RecommendedDongsSortedByScoreDescending() {
        // given - 15개의 추천동 생성 (다양한 점수로)
        List<RecommendedDong> testDongs = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            testDongs.add(RecommendedDong.builder()
                    .dongCode(1000 + i)
                    .dongName("테스트동" + i)
                    .score(BigDecimal.ZERO) // 초기 점수 (실제 계산에서는 무시됨)
                    .build());
        }

        // 계약 데이터 mock 설정 - 계약 건 수를 95, 90, 85...로 설정하고 maxCnt를 100으로 설정
        List<ContractCntDTO> contractList = new ArrayList<>();
        contractList.add(new ContractCntDTO(1016, 100L)); // maxCnt를 100으로 설정하기 위한 더미 데이터
        for (int i = 1; i <= 15; i++) {
            contractList.add(new ContractCntDTO(1000 + i, 100L - i * 5)); // 95, 90, 85, ..., 25
        }

        when(mockRepository.findContractCntByPreferenceInDongCodes(testSurvey, contractList.stream().map(ContractCntDTO::getDongCode).toList()))
                .thenReturn(contractList);

        // when
        List<RecommendedDong> result = calculator.calcHousePriceScore(testSurvey, testDongs);

        // then
        // 1. 결과가 정확히 10개인지 확인
        Assertions.assertThat(result).hasSize(10);

        // 2. 점수가 내림차순으로 정렬되어 있는지 확인
        for (int i = 0; i < result.size() - 1; i++) {
            Assertions.assertThat(result.get(i).getScore())
                    .isGreaterThanOrEqualTo(result.get(i + 1).getScore());
        }

        // 3. 가장 높은 점수가 첫 번째에 있는지 확인 (95.0)
        Assertions.assertThat(result.getFirst().getScore()).isEqualTo(new BigDecimal("95.0"));

        // 4. 가장 낮은 점수가 마지막에 있는지 확인 (50.0)
        Assertions.assertThat(result.getLast().getScore()).isEqualTo(new BigDecimal("50.0"));
    }
}