package com.itset.itcenteamproject.domain.user;

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
    //설문 관련은 LoginSuccessHandler에서 사용
    //세션 관련
    private final SessionUserService sessionUserService;

    //로그인 테스트용
    @Operation(summary = "로그인 테스트")
    @PostMapping("/auth/login")
    public LoginResponseDTO login(
            @RequestBody LoginRequestDTO dto,
            HttpSession session
    ) {
        User user = userService.login(dto);
        sessionUserService.login(session, user.getId());

        return LoginResponseDTO.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    //회원가입
    @Operation(summary = "회원가입")
    @PostMapping("/auth/signup")
    public ResponseEntity<Void> signup(
            @RequestBody SignupRequestDTO dto
    ) {
        userService.signup(dto);

        return ResponseEntity.status(201).build();
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

    //닉네임 중복 확인
    @GetMapping("/users/check-nickname")
    public DupCheckResponseDTO checkNickname(
            @RequestParam String nickname
    ) {
        userService.checkNicknameDuplicate(nickname);

        return new DupCheckResponseDTO(true);
    }

    //로그인 된 사용자 정보 조회(세션 유지 확인용)
    @GetMapping("/me")
    @Operation(summary = "유저 정보 조회",description = "세션 유저의 정보 조회")
    public UserResponseDTO me() {
        Long userId = sessionUserService.getLoginUserId();
        User user = userService.findById(userId);

        return new UserResponseDTO(user);
    }
}
