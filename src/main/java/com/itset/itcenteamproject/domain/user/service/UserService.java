package com.itset.itcenteamproject.domain.user.service;

import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import com.itset.itcenteamproject.domain.user.dto.LoginRequestDTO;
import com.itset.itcenteamproject.domain.user.dto.SignupRequestDTO;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    //영문 포함, 숫자 포함, 6~12자
    private final String ID_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,12}$";
    //영문 포함, 숫자 포함, 특수문자 포함, 6자 이상(공백 제외)
    private final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{6,}$";

    //회원가입
    public void signup(SignupRequestDTO dto) {
        //회원가입 전 검사
        validateSignup(dto);

        User user = User.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname().trim())
                .build();
        userRepository.save(user);
    }

    //회원가입 버튼 눌렀을 때(회원가입 직전) 검사
    private void validateSignup(SignupRequestDTO dto) {
        // 아이디 형식을 검사한다.
        if (dto.getLoginId() == null || !dto.getLoginId().matches(ID_REGEX)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_ID);
        }

        // 비밀번호 형식을 검사한다.
        if (dto.getPassword() == null || !dto.getPassword().matches(PASSWORD_REGEX)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_PW);
        }

        // 닉네임 입력 여부를 검사한다.
        if (dto.getNickname() == null || dto.getNickname().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 로그인 아이디 중복을 검사한다.
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 닉네임은 앞뒤 공백을 제거한 값으로 중복 검사한다.
        String nickname = dto.getNickname().trim();

        // 닉네임은 10자 이내
        if (nickname.length() > 10) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_NAME);
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    //로그인. 필요없음(삭제 예정)
    public User login(LoginRequestDTO dto) {
        User user = userRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(() ->
                        new CustomException(ErrorCode.LOGIN_FAILED));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
        return user;
    }

    //로그인 아이디 중복검사
    public void checkLoginIdDuplicate(String loginId) {
        String trimmedLoginId = loginId.trim();
        if (userRepository.existsByLoginId(trimmedLoginId)) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

    //닉네임 중복검사
    public void checkNicknameDuplicate(String nickname) {
        String trimmedNickname = nickname.trim();
        if (userRepository.existsByNickname(trimmedNickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    //userId로 DB(User) 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }
}
