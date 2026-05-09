package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.survey.SurveyRepository;
import com.itset.itcenteamproject.domain.user.dto.*;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import com.itset.itcenteamproject.domain.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;
    private final SurveyRepository surveyRepository;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDTO dto
    ) {
        userService.signup(dto);

        return ResponseEntity.status(201).build();
    }

    //로그인
    @PostMapping("/auth/login")
    public LoginResponseDTO login(
            @RequestBody LoginRequestDTO dto,
            HttpSession session
    ) {
        User user = userService.login(dto);
        session.setAttribute("loginUser", user.getId());
        session.setMaxInactiveInterval(120);

        //설문 여부 확인 후 페이지 이동(프론트)
        boolean surveyCompleted = surveyRepository.existsByUserId(user.getId());
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return new LoginResponseDTO(
                user.getLoginId(),
                user.getNickname(),
                surveyCompleted,
                redirectPath
        );
    }

    //로그인 아이디 중복 확인
    @GetMapping("/users/check")
    public DupCheckResponseDTO checkLoginId(
            @RequestParam String loginId
    ) {
        boolean duplicated = userService.checkLoginIdDuplicate(loginId);
        if (duplicated) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        return new DupCheckResponseDTO(true);
    }

    //로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        //인증 체크
        getLoginUser(session);
        session.invalidate();

        return ResponseEntity.noContent().build();
    }

    //로그인 된 사용자 정보 조회(세션 유지 테스트용)
    @GetMapping("/me")
    public UserResponseDTO me(HttpSession session) {
        Long userId = getLoginUser(session);
        User user = userService.findById(userId);

        return new UserResponseDTO(user);
    }

    //의도하지 않은 경로로 대시보드 진입 방지
    //프론트에서 대시보드 페이지 진입 전 호출
    @GetMapping("/auth/survey-check")
    public SurveyCheckResponseDTO checkSurvey(HttpSession session) {
        Long userId = getLoginUser(session);

        boolean surveyCompleted = surveyRepository.existsByUserId(userId);
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return new SurveyCheckResponseDTO(surveyCompleted, redirectPath);
    }


    //인증 체크(세션에 loginUser가 있는지)
    private Long getLoginUser(HttpSession session) {
        //로그인 상태 -> userid 반환
        Long userId = (Long) session.getAttribute("loginUser");
        //세션 없음 -> 401예외
        if (userId == null) {
            throw new CustomException(ErrorCode.SESSION_EXPIRED);
        }
        return userId;
    }
}
