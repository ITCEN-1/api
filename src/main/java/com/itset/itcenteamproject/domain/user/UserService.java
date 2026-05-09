package com.itset.itcenteamproject.domain.user;

import com.itset.itcenteamproject.domain.user.dto.LoginRequestDTO;
import com.itset.itcenteamproject.domain.user.dto.SignupRequestDTO;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.transaction.Transactional;
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

    //회원가입
    public void signup(SignupRequestDTO dto) {
        //회원가입 전 검사
        validateSignup(dto);

        User user = User.builder()
                .loginId(dto.getLoginId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build();
        userRepository.save(user);
    }

    //회원가입 버튼 눌렀을 때(회원가입 직전) 검사
    private void validateSignup(SignupRequestDTO dto) {
        //아이디 형식 검사
        if (!dto.getLoginId().matches(ID_REGEX)) {
            throw new CustomException(ErrorCode.INVALID_LOGIN_ID);
        }
        //아이디 중복 검사, true면 예외
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        //닉네임 중복 검사, true면 예외
        /*if (userRepository.existsByNickname(dto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }*/
    }

    //로그인
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
    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    //userId로 DB(User) 조회, 세션에 userId만 있음
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    //설문 완료 설정(설문 확인 테스트용)
    @Transactional
    public void completeSurvey(Long userId) {
        User user = findById(userId);
        user.completeSurvey();
    }
}
