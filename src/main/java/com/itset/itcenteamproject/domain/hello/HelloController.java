package com.itset.itcenteamproject.domain.hello;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="hello",description = "hello 관련 API")
@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
public class HelloController {

    private final HelloService helloService;

    @Operation(summary = "hello 조회", description = "요청값의 id에 해당하는 hello 가 있는지 조회합니다")
    @GetMapping
    public HelloResponseDTO getHello(@RequestParam Long helloId){
        return helloService.getHello(helloId);
    }
}
