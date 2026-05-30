package com.itset.itcenteamproject.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 에러코드 종류가 많아지면 클래스 나누기
    // 여기 정의되지 않은 에러는 GlobalExceptionHandler에 의해 500 에러 처리됨

    NOTFOUND_HELLO(HttpStatus.NOT_FOUND,"HEL001","해당 Id에 해당하는 Hello를 찾을 수 없습니다."),

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류입니다."),

    // 게시판, 댓글관련
    REQUIRED_TITLE(HttpStatus.BAD_REQUEST, "C004", "제목을 입력해주세요."),
    REQUIRED_CONTENT(HttpStatus.BAD_REQUEST, "C005", "본문을 입력해주세요."),
    REQUIRED_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "C006", "댓글 내용을 입력해주세요."),
    COMMENT_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "C007", "댓글은 500자 이하로 입력해주세요."),

    // User관련 추가
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "U001", "잘못된 요청입니다."),
    INVALID_LOGIN_ID(HttpStatus.BAD_REQUEST, "U002", "아이디는 6~12자 영문+숫자 조합이어야 합니다."),
    INVALID_LOGIN_PW(HttpStatus.BAD_REQUEST, "U003", "비밀번호는 6자 이상, 영문+숫자+특수문자 조합(공백 제외)이어야 합니다."),
    INVALID_LOGIN_NAME(HttpStatus.BAD_REQUEST, "U004", "닉네임은 10자 이내여야 합니다."),


    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "U005", "이미 사용 중인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U006", "이미 사용 중인 닉네임입니다."),

    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "U007", "아이디 또는 비밀번호가 올바르지 않습니다."),
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "U008", "세션이 만료되었습니다."),

    NOT_FOUND_USER(HttpStatus.NOT_FOUND,"U009","존재하지 않는 유저입니다"),

    // 설문 관련
    INVALID_RENTAL_FILED(HttpStatus.BAD_REQUEST,"SUR001","정상적인 전세 또는 월세+보증금 입력이 아닙니다"),
    INVALID_MONTHLY_FILED(HttpStatus.BAD_REQUEST,"SUR002","정상적인 월세,보증금 입력이 아닙니다, 월세최소+월세최대+보증금최소+보증금최대를 모두 입력하세요"),
    INVALID_JEONSE_FILED(HttpStatus.BAD_REQUEST,"SUR003","정상적인 전세 입력이 아닙니다, 전세최소+전세최대를 모두 입력하세요"),
    NOT_FOUND_SURVEY(HttpStatus.NOT_FOUND,"SUR004","유저가 가진 설문이 없습니다"),
    INVALID_MIN_MAX_VALUE(HttpStatus.BAD_REQUEST, "SUR005", "최소 금액이 최대 금액보다 작아야합니다."),

    // DongLocation 관련
    REQUIRED_DONG_CODE(HttpStatus.BAD_REQUEST, "DON000", "동을 선택해주세요."),
    INVALID_DONG_CODE(HttpStatus.BAD_REQUEST,"DON001","유효하지 않은 동 코드입니다. (1111010100 ~ 1174011000)"),

    // 통근점수 산정 관련
    ODSAY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM002", "오디세이 API 호출에 실패했습니다."),
    ODSAY_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM003", "오디세이 응답 파싱에 실패했습니다."),
    ODSAY_BAD_API_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "COM004", "오디세이 API 호출 결과에 문제가 있습니다"),

    // 카카오 API 관련
    KAKAO_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"KA001","카카오 API 요청이 실패했습니다"),
    KAKAO_API_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"KA002","카카오 API 요청이 실패했습니다"),
    INVALID_WORKPLACE_ADDRESS(HttpStatus.BAD_REQUEST,"KA003" , "직장 주소를 찾을 수 없습니다. 주소를 다시 확인해주세요."),

    // 전/월세 관련
    NO_CONTRACT_DATA(HttpStatus.NOT_FOUND, "HOU001", "해당하는 전/월세 계약건 수가 존재하지 않습니다"),

    // 히스토리 관련
    NO_HISTORY_DATA(HttpStatus.NOT_FOUND, "HI001", "설문에 해당하는 히스토리가 존재하지 않습니다.");

    private final HttpStatus status; //헤더 상태코드로 들어감
    private final String code; //errorCode.getCode 로 사용
    private final String message;

    /**
     * 맴버 변수에 따른 응답 예시
     * HTTP/1.1 400 Bad Request           <-- status
     * Content-Type: application/json
     *
     * {
     *   "code": "C001",                <-- code
     *   "message": "잘못된 입력 값입니다.", <-- message
     *   "data": null
     * }
     */
}
