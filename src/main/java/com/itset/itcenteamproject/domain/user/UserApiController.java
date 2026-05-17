package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.survey.SurveyService;
import com.itset.itcenteamproject.domain.user.dto.*;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import com.itset.itcenteamproject.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {
    //회원가입, 로그인 관련
    private final UserService userService;
    //설문 관련
    private final SurveyService surveyService;
    //세션 관련
    private final SessionUserService sessionUserService;

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
        sessionUserService.login(session, user.getId());

        // 관리자는 설문 여부와 관계없이 관리자 페이지로 이동
        if (user.isAdmin()) {
            return LoginResponseDTO.builder()
                    .loginId(user.getLoginId())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .surveyCompleted(false)
                    .redirectPath("/admin")
                    .build();
        }

        //일반 사용자는 설문 여부 확인 후 페이지 이동(프론트)
        boolean surveyCompleted = surveyService.hasSurvey(user.getId());
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return LoginResponseDTO.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .surveyCompleted(surveyCompleted)
                .redirectPath(redirectPath)
                .build();
    }

    //로그인 아이디 중복 확인
    @GetMapping("/users/check")
    public DupCheckResponseDTO checkLoginId(
            @RequestParam String loginId
    ) {
        userService.checkLoginIdDuplicate(loginId);

        return new DupCheckResponseDTO(true);
    }

    //로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        //인증 체크
        sessionUserService.logout(session);

        return ResponseEntity.noContent().build();
    }

    //로그인 된 사용자 정보 조회(세션 유지 테스트용)
    @GetMapping("/me")
    public UserResponseDTO me(HttpSession session) {
        Long userId = sessionUserService.getLoginUserId(session);
        User user = userService.findById(userId);

        return new UserResponseDTO(user);
    }

    //의도하지 않은 경로로 대시보드 진입 방지
    //프론트에서 대시보드 페이지 진입 전 호출
    @Operation(description = "의도하지 않은 경로로 대시보드 진입 방지," +
            " 프론트에서 대시보드 페이지 진입 전 호출", summary = "대시보드 진입 전 체크" )
    @GetMapping("/auth/survey-check")
    public SurveyCheckResponseDTO checkSurvey(HttpSession session) {
        Long userId = sessionUserService.getLoginUserId(session);
        boolean surveyCompleted = surveyService.hasSurvey(userId);
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return new SurveyCheckResponseDTO(surveyCompleted, redirectPath);
    }
}
