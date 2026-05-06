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

    // User (예시)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다.");

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