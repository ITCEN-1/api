package com.itset.itcenteamproject.domain.admin;

import com.itset.itcenteamproject.domain.board.BoardRepository;
import com.itset.itcenteamproject.domain.dashboard.service.LocationService;
import com.itset.itcenteamproject.domain.history.DongCodeCountDto;
import com.itset.itcenteamproject.domain.history.HistoryItemRepository;
import com.itset.itcenteamproject.domain.survey.PreferenceLevel;
import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int RECENT_DAYS = 10;

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final BoardRepository boardRepository;
    private final HistoryItemRepository historyItemRepository;
    private final LocationService locationService;

    //유저 개수
    public long getUserCount(){ return userRepository.count(); }
    //설문 개수
    public long getSurveyCount(){ return surveyRepository.count(); }
    //게시글 개수
    public long getBoardCount(){ return boardRepository.count(); }

    //상단 (누적 유저/설문/게시글) 집계
    public List<KpiCard> getKpis(){
        return List.of(
                new KpiCard("누적 유저", formatCount(getUserCount())),
                new KpiCard("누적 설문", formatCount(getSurveyCount())),
                new KpiCard("누적 게시글", formatCount(getBoardCount()))
        );
    }

    public record KpiCard(String label, String value) {}

    private static String formatCount(long n){
        return String.format("%,d", n);
    }

    //최근 10일간 일별 가입자 추이
    public List<DailySignupPoint> getRecentDailySignups() {
        LocalDate today = LocalDate.now();//YYYY-MM-DD
        List<DailySignupPoint> points = new ArrayList<>(RECENT_DAYS); //최근 날짜 범위를 수정 가능하게 설계
        for (int i = RECENT_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = userRepository.countByCreatedAtInDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            points.add(new DailySignupPoint(date.format(DateTimeFormatter.ofPattern("M/d")), count));
        }
        return points;
    }

    public record DailySignupPoint(String label, long count) {} // (n월n일 , 3) 형태

    //가장 많이 추천된 상위 10개 동
    public List<TopDongRow> getTop10DongCode(){
        Pageable pageable = PageRequest.of(0, 10); //상위 10개 동 까지만 추출
        List<DongCodeCountDto> raw = historyItemRepository.findTop10DongCode(pageable);

        //동 코드 -> 한글 동 이름 매핑
        List<Integer> codes = raw.stream()
                .map(d->d.dongCode())//dongCode()는 getter 같은 역할을 한다, record 클래스가 자동으로 생성하는 메소드임
                .toList();

        //동 코드에 대한 이름 변환 키-벨류 쌍을 가져온다
        Map<Integer, String> codeToName = locationService.getDongNamesByDongCodes(codes);

        //키-벨류 쌍을 이용해 랭킹 순서를 유지한 채 한글 이름으로 변환 (매핑 실패 시 코드 그대로 표시)
        return raw.stream()
                .map(d -> new TopDongRow(codeToName.getOrDefault(d.dongCode(), String.valueOf(d.dongCode())), d.cnt()))
                .toList();
    }

    public record TopDongRow(String name, long count) {}

    //모든 인프라 선호도 설문 평균
    public List<SurveyResultGroup> getSurveyResult(){
        List<SurveyResultGroup> surveyResultGroups = new ArrayList<>();
        for(Map.Entry<String, String> entry : infraTypeMap.entrySet()){// 각 요소에 대하여 상,중,하 개수 집계
            String infraCode = entry.getKey();
            String infraLabel = entry.getValue();

            long high   = countByInfraAndLevel(infraCode, PreferenceLevel.HIGH);
            long middle = countByInfraAndLevel(infraCode, PreferenceLevel.MIDDLE);
            long low    = countByInfraAndLevel(infraCode, PreferenceLevel.LOW);
            long sum = high + middle + low;

            long highRate = 0, middleRate = 0, lowRate = 0;

            // 0으로 나눠서 발생하는 에러 체크+정수 반올림
            if(sum > 0){
                highRate = Math.round((double)high/sum * 100);
                middleRate = Math.round((double)middle/sum * 100);
                lowRate = Math.round((double)low/sum * 100);
            }

            surveyResultGroups.add(new SurveyResultGroup(infraLabel, highRate, middleRate, lowRate));
        }
        return surveyResultGroups;
    }

    // 인프라 코드별로 알맞은 Survey 컬럼의 선호도 응답수를 조회
    // NOTE: JPQL은 컬럼명을 파라미터로 못 받아서 이렇게 따로따로 보낼수밖에 없음
    private long countByInfraAndLevel(String infraCode, PreferenceLevel level){
        long count = 0L;
        switch (infraCode){
            case "hospital":
                count=surveyRepository.countByPreferenceHospital(level);
                break;
            case "largeStore":
                count=surveyRepository.countByPreferenceLargeStore(level);
                break;
            case "subway":
                count=surveyRepository.countByPreferenceSubway(level);
                break;
            case "library":
                count=surveyRepository.countByPreferenceLibrary(level);
                break;
        }
        return count;
    }

    public record SurveyResultGroup(String infraType, long highRate, long middleRate, long lowRate){}
    Map<String, String> infraTypeMap = new LinkedHashMap<>() {{//병원/대형마트/도서관/지하철" 순서 유지를 위해 Map 대신 LinkedHashMap 사용
        put("hospital","병원");
        put("largeStore","대형마트");
        put("library","도서관");
        put("subway","지하철");
    }};
}
