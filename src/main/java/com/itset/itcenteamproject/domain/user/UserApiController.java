package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.survey.SurveyService;
import com.itset.itcenteamproject.domain.user.dto.*;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import com.itset.itcenteamproject.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name="users",description = "유저,인증 관련 api")
public class UserApiController {
    //회원가입, 로그인 관련
    private final UserService userService;
    //설문 관련
    private final SurveyService surveyService;
    //세션 관련
    private final SessionUserService sessionUserService;

    //회원가입
    @Operation(summary = "회원가입")
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDTO dto
    ) {
        userService.signup(dto);

        return ResponseEntity.status(201).build();
    }

    //로그인
    @Operation(summary = "로그인")
    @PostMapping("/auth/login")
    public LoginResponseDTO login(
            @RequestBody LoginRequestDTO dto,
            HttpSession session
    ) {
        User user = userService.login(dto);
        sessionUserService.login(session, user.getId());

        //설문 여부 확인 후 페이지 이동(프론트)
        boolean surveyCompleted = surveyService.hasSurvey(user.getId());
        String redirectPath = surveyCompleted ? "/dashboard" : "/surveys";

        return LoginResponseDTO.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .surveyCompleted(surveyCompleted)
                .redirectPath(redirectPath)
                .build();
    }

    //로그아웃
    @Operation(summary = "로그아웃")
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        //인증 체크
        sessionUserService.logout(session);

        return ResponseEntity.noContent().build();
    }

    //로그인 아이디 중복 확인
    @Operation(summary = "동일 아이디 조회")
    @GetMapping("/users/check")
    public DupCheckResponseDTO checkLoginId(
            @RequestParam String loginId
    ) {
        userService.checkLoginIdDuplicate(loginId);

        return new DupCheckResponseDTO(true);
    }

    //로그인 된 사용자 정보 조회(세션 유지 테스트용)
    @GetMapping("/me")
    @Operation(summary = "[Test] 세션 유저의 정보 조회")
    public UserResponseDTO me(HttpSession session) {
        Long userId = sessionUserService.getLoginUserId(session);
        User user = userService.findById(userId);

        return new UserResponseDTO(user);
    }
}
