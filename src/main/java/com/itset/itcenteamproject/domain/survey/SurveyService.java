package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.itset.itcenteamproject.exception.ErrorCode.NOT_FOUND_USER;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createSurvey(SurveyCreateRequest request,Long userId){

        //유효성 검증
        validateSurvey(request);

        //설문에 FK 로 들어갈 유저 엔티티 가져오기
        User user = userRepository.findById(userId).orElseThrow(()-> new CustomException(NOT_FOUND_USER));

        //설문 객체 생성 저장
        Survey survey = surveyRepository.save(request.toEntity(user));

        if(request.getSelectedDistricts() != null){
            request.getSelectedDistricts().forEach(districtName -> {
                SurveySelectedDistrict surveySelectedDistrict = SurveySelectedDistrict.builder()
                        .survey(survey)
                        .districtName(districtName)
                        .build();
                survey.getSurveySelectedDistrictList().add(surveySelectedDistrict);
                // 연관관계 cascade 덕분에 트랜잭션 내에서 한 번에 커밋됨
            });
        }
        return survey.getId();
    }

    //정상적인 설문(전세월세보증금)인지 검사:
    private void validateSurvey(SurveyCreateRequest request) {

        boolean hasJeonseMin = request.getJeonseMin() != null;
        boolean hasJeonseMax = request.getJeonseMax() != null;
        boolean hasMonthlyMin = request.getMonthlyMin() != null;
        boolean hasMonthlyMax = request.getMonthlyMax() != null;
        boolean hasDepositMin = request.getDepositMin() != null;
        boolean hasDepositMax = request.getDepositMax() != null;

        boolean isJeonseGroup = hasJeonseMin || hasJeonseMax;
        boolean isMonthlyGroup = hasMonthlyMin || hasMonthlyMax || hasDepositMin || hasDepositMax;

        // 둘 다 없거나 둘 다 있음
        if (!isJeonseGroup && !isMonthlyGroup || isJeonseGroup && isMonthlyGroup) {
            throw new CustomException(ErrorCode.INVALID_RENTAL_FILED);
        }

        // 전세 쌍 불완전
        if (isJeonseGroup && !(hasJeonseMin && hasJeonseMax)) {
            throw new CustomException(ErrorCode.INVALID_JEONSE_FILED);
        }

        // 월세/보증금 묶음 불완전
        if (isMonthlyGroup && !(hasMonthlyMin && hasMonthlyMax && hasDepositMin && hasDepositMax)) {
            throw new CustomException(ErrorCode.INVALID_MONTHLY_FILED);
        }
    }
    //설문여부 확인
    public boolean hasSurvey(Long userId) {
        return surveyRepository.existsByUserId(userId);
    }
}
