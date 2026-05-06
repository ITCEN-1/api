package com.itset.itcenteamproject.response;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private String code;
    private String message;
    private T content;

    private ApiResponse(String code, String message, T content) {
        this.code = code;
        this.message = message;
        this.content = content;
    }

    // 성공 응답
    // data 에는 클라이언트가 요청한 값이 들어감
    // 제네릭 메소드임
    public static <T> ApiResponse<T> success(T content) {
        return new ApiResponse<>("SUCCESS", "요청이 성공하였습니다.", content);
    }

    // 에러 응답
    // 상태 메시지는 errorCode 에 맵핑된 메시지값
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    // 에러 응답
    // 상태 메시지를 직접 기입하는 경우
    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, null);
    }
}