package com.itset.itcenteamproject.domain.dashboard.service;

import com.itset.itcenteamproject.domain.dashboard.client.KakaoGeocodingClient;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.global.vo.Coordinate;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_SURVEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorOrchestrator {

    private final SurveyRepository surveyRepository;
    private final InfraScoreCalculator infraScoreCalculator;
    private final HouseScoreCalculator houseScoreCalculator;
    private final CommuteScoreCalculator commuteScoreCalculator;
    private final KakaoGeocodingClient kakaoGeocodingClient;
    private final LocationService locationService;


    //현재 유저의 가장 최근 설문 내용을 기반으로 점수를 산정
    @Transactional
    public List<RecommendedDong> getRankingListFromCurrentSurvey(Long userId){

        // 유저 Id 정보에서 설문 정보 가져오기
        Survey survey = surveyRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElseThrow(()-> new CustomException(NOT_FOUND_SURVEY));

        // surveySelectedDistrictList ['마포구','송파구',...] -> 동 리스트로 변환 with DongLocationRepository
        List<Integer> filteredDongCodes= locationService.getDongCodesBySurvey(survey);
        log.info("[Perf] filteredDongCodes size={}", filteredDongCodes.size());

        //점수 산정

        //1. 인프라 점수 산정 100 -> 100
        List<RecommendedDong> infraStepRecommendedDongList = infraScoreCalculator.calculateTopDongs(survey,filteredDongCodes);

        //2. 인프라 점수에 집값 점수 추가 100 -> 10
        List<RecommendedDong> houseStepRecommendedDongList = houseScoreCalculator.calcHousePriceScore(survey,infraStepRecommendedDongList);

        //3(optional). WorkplaceAddress 가 있다면 인프라+집값 점수로 산정된 상위 10개 동에 통근시간 점수 추가 10->10
        if(survey.getWorkplaceAddress()!=null){
            Coordinate workplaceCoordinate=kakaoGeocodingClient.addressToCoordinate(survey.getWorkplaceAddress());

            List<RecommendedDong> commuteStepRecommendedDongList = commuteScoreCalculator.calculate(workplaceCoordinate,houseStepRecommendedDongList);

            List<RecommendedDong> result = scoreDongList(commuteStepRecommendedDongList);

            return result;
        }

        //최종 결과에 순위 부여
        List<RecommendedDong> result = scoreDongList(houseStepRecommendedDongList);

        return result;
    }

    // 동 리스트의 순위를 매기는 메소드
    private List<RecommendedDong> scoreDongList(List<RecommendedDong> unscoredDongs){
        List<RecommendedDong> dongList= unscoredDongs.stream().sorted().toList();
        //정렬된 순서대로 랭킹 부여
        for(int i=0;i<dongList.size();i++){
            dongList.get(i).setRanking(i+1);
        }
        return dongList;
    }
}
