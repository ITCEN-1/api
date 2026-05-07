package com.itset.itcenteamproject.domain.hello;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Hello {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private Integer age;
}
