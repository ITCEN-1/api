package com.itset.itcenteamproject.domain.survey;

import com.itset.itcenteamproject.domain.survey.dto.SurveyCreateRequest;
import com.itset.itcenteamproject.domain.survey.dto.SurveyCreateResponse;
import com.itset.itcenteamproject.domain.survey.dto.SurveyDTO;
import com.itset.itcenteamproject.domain.survey.entity.Survey;
import com.itset.itcenteamproject.domain.survey.entity.SurveySelectedDistrict;
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
    public SurveyCreateResponse createSurvey(SurveyCreateRequest request, Long userId){

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

        return SurveyCreateResponse.from(survey);
    }

    //정상적인 설문(전세월세보증금)인지 검사:
    private void validateSurvey(SurveyCreateRequest request) {
        // A. 필드 존재 여부 추출
        boolean hasJeonse = request.getJeonseMin() != null || request.getJeonseMax() != null;
        boolean hasMonthly = request.getMonthlyMin() != null || request.getMonthlyMax() != null;
        boolean hasDeposit = request.getDepositMin() != null || request.getDepositMax() != null;

        // B. 전세/월세 그룹 판별
        boolean isJeonseGroup = hasJeonse;
        boolean isMonthlyGroup = hasMonthly || hasDeposit;

        // 1. 배타적 선택 검증 (둘 다 없거나 둘 다 있는 경우)
        if (isJeonseGroup == isMonthlyGroup) {
            throw new CustomException(ErrorCode.INVALID_RENTAL_FILED);
        }

        // 2. 전세 상세 검증
        if (isJeonseGroup) {
            // 필수 값 누락 체크
            if (request.getJeonseMin() == null || request.getJeonseMax() == null) {
                throw new CustomException(ErrorCode.INVALID_JEONSE_FILED);
            }
            // 대소 관계 체크 (기존 Validator 로직)
            if (request.getJeonseMin() > request.getJeonseMax()) {
                throw new CustomException(ErrorCode.INVALID_MIN_MAX_VALUE); // 에러코드 정의 필요
            }
        }

        // 3. 월세 상세 검증
        if (isMonthlyGroup) {
            // 필수 값 누락 체크 (보증금/월세 모두 있어야 함)
            if (request.getMonthlyMin() == null || request.getMonthlyMax() == null ||
                    request.getDepositMin() == null || request.getDepositMax() == null) {
                throw new CustomException(ErrorCode.INVALID_MONTHLY_FILED);
            }
            // 보증금 대소 관계 체크
            if (request.getDepositMin() > request.getDepositMax()) {
                throw new CustomException(ErrorCode.INVALID_MIN_MAX_VALUE);
            }
            // 월세 대소 관계 체크
            if (request.getMonthlyMin() > request.getMonthlyMax()) {
                throw new CustomException(ErrorCode.INVALID_MIN_MAX_VALUE);
            }
        }
    }
    //과거 설문을 봐도 본인 설문만 허용
    public Survey findByIdAndUserId(Long surveyId, Long userId) {
        return surveyRepository.findByIdAndUserId(surveyId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SURVEY));
    }

    //설문여부 확인
    public boolean hasSurvey(Long userId) {
        return surveyRepository.existsByUserId(userId);
    }
}
