package com.itset.itcenteamproject.exception;

import com.itset.itcenteamproject.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 직접 정의한 CustomException을 처리합니다.
     * HTTP/1.1 400 Bad Request           <-- [1] ResponseEntity.status()가 만든 헤더
     * Content-Type: application/json
     *
     * {                                  <-- [2] ApiResponse.error()가 만든 바디
     *   "code": "C001",
     *   "message": "잘못된 입력 값입니다.",
     *   "data": null
     * }
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("커스텀 에러 발생: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode));
    }

    /**
     * 사전에 정의한 CustomException 에러가 아닌 모든 예외를 처리.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예기치 않은 예외: ", e);
        // 예외 객체 'e' 가 마지막에 오면 스택 트레이스를 다 보여줌, Slf4j의 특수 문법
        // {} + 같은걸로 쓰면 스택트레이스가 짤려서 나옴
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 에러로 퉁침
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}