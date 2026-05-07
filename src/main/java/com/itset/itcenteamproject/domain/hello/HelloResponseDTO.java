package com.itset.itcenteamproject.domain.hello;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HelloResponseDTO {
    private Long id;
    private String name;
    private Integer age;
}
