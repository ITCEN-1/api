package com.itset.itcenteamproject.commuteScoreCalculatorTest;

import com.itset.itcenteamproject.domain.dashboard.CommuteScoreCalculator;
import com.itset.itcenteamproject.domain.dashboard.model.RecommendedDong;
import com.itset.itcenteamproject.domain.infra.Coordinate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 테스트 코드 작성법 참고한 곳
 * https://mangkyu.tistory.com/144
 */
@SpringBootTest
public class CommuteScoreCalculatorIntegrationTest {

    @Autowired
    private CommuteScoreCalculator commuteScoreCalculator;

    @Test
    @DisplayName("오디세이 api 호출 테스트")
    void getCommuteMinutesByOdsayTest(){
        //given
        Coordinate workPlaceCoordinate = new Coordinate(126.9923298,37.57038842);
        Integer destintationDongCode=1171010700;//가락동

        //when
        int min=commuteScoreCalculator.getCommuteMinutesByOdsay(workPlaceCoordinate,destintationDongCode);

        //then
        System.out.println("통근시간:" + min + "분!!!!");
        assertThat(min).isPositive();//값이 0보다 크면 통과
    }

    @Test
    @DisplayName("CommuteScoreCalculator.calculate 테스트")
    void calculateTest(){
        //given
        Coordinate workPlaceCoordinate = new Coordinate(126.9923298,37.57038842);
        RecommendedDong rd1= RecommendedDong.builder()
                .ranking(1)
                .dongCode(1117013000)
                .dongName("이태원동")
                .latitude(37.5402662100000)
                .longitude(126.9915273000000)
                .score(BigDecimal.valueOf(80))
                .build();

        RecommendedDong rd2= RecommendedDong.builder()
                .ranking(1)
                .dongCode(1171010700)
                .dongName("가락동")
                .latitude(37.4965421700000)
                .longitude(127.1057294000000)
                .score(BigDecimal.valueOf(80))
                .build();

        RecommendedDong rd3= RecommendedDong.builder()
                .ranking(1)
                .dongCode(1144012200)
                .dongName("합정동")
                .latitude(37.5516618100000)
                .longitude(126.9117564000000)
                .score(BigDecimal.valueOf(80))
                .build();

        List<RecommendedDong> rdList= Arrays.asList(rd1,rd2,rd3);
        //when
        List<RecommendedDong> addScoredList=commuteScoreCalculator.calculate(workPlaceCoordinate,rdList);

        //then
        addScoredList.forEach(en->System.out.println(en.toString())); // 디버깅용,콘솔에 점수가산된 추천동객체 반환
        addScoredList.forEach(rd -> {
            assertThat(rd.getScore()).isGreaterThan(BigDecimal.valueOf(90));  // 점수가 90보다 크면 제대로 가산된거임
        });

    }




}
