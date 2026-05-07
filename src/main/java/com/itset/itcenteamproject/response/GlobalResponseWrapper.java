package com.itset.itcenteamproject.response;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseWrapper implements ResponseBodyAdvice<Object> {

    //ApiResponse로 감싸면 안되는 응답들을 걸러냅니다
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType){

        //이미 ApiResponse로 감싼 경우 스킵
        if (ApiResponse.class.isAssignableFrom(returnType.getParameterType()))
            return false;

        //이미 ResponseEntity로 감싼 경우 스킵
        if(ResponseEntity.class.isAssignableFrom(returnType.getParameterType()))
            return false;

        /* converterType 관련 검증 */
        //객체 to JSON 이 아닌 경우를 제외하고는 전부 스킵, 따라서 JSON컨버터일때만 통과
        if( !MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) )
            return false;

        return true;
    }

    //컨트롤러 응답값이 HTTP응답으로 전환되기 전 공통 응답 객체로 wrapping 한다
    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        return ApiResponse.success(body);
    }
}
