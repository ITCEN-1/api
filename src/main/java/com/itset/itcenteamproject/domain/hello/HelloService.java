package com.itset.itcenteamproject.domain.hello;

import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;

    public HelloResponseDTO getHello(Long helloId){
        Hello hello = helloRepository
                .findById(helloId)
                .orElseThrow(()->new CustomException(ErrorCode.NOTFOUND_HELLO));

        return new HelloResponseDTO(hello.getId(),hello.getName(),hello.getAge());
    }
}
