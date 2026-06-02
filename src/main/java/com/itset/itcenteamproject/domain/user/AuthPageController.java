package com.itset.itcenteamproject.domain.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//API(/api/auth/login, /api/signup)는 기존 UserApiController를 그대로 쓰고 이 컨트롤러는 화면 라우팅만 담당
@Controller
@RequestMapping("/auth")
public class AuthPageController {
    @Value("${frontserver.url}") private String frontServerUrl;

    @GetMapping("/login") public String loginPage() { return "redirect:" + frontServerUrl + "/login"; }
    @GetMapping("/signup") public String signupPage() { return "redirect:" + frontServerUrl + "/signup"; }
}
