package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.user.dto.*;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;

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
        session.setMaxInactiveInterval(60);

        return new LoginResponseDTO(
                user.getLoginId(),
                user.getNickname()
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

    //설문여부 확인
    @GetMapping("/auth/survey-status")
    public Boolean surveyStatus(HttpSession session) {
        Long userId = getLoginUser(session);
        User user = userService.findById(userId);

        return user.getHasSurvey();
    }

    //설문 완료하기(설문여부 확인 테스트용)
    @PostMapping("/survey/complete")
    public ResponseEntity<Void> completeSurvey(HttpSession session) {
        Long userId = getLoginUser(session);
        userService.completeSurvey(userId);

        return ResponseEntity.ok().build();
    }

    //인증 체크
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
