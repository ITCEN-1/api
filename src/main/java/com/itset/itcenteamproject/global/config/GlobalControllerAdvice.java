package com.itset.itcenteamproject.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${frontserver.url:}")
    private String frontServerUrl;

    @ModelAttribute("frontServerUrl")
    public String frontServerUrl() {
        return frontServerUrl;
    }
}

