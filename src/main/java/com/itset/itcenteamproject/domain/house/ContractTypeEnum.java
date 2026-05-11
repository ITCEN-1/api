package com.itset.itcenteamproject.domain.house;

import lombok.Getter;

@Getter
public enum ContractTypeEnum {
    WOLSE("wolseRepository"),
    JEONSE("jeonseRepository");

    private final String beanName;

    ContractTypeEnum(String beanName) {
        this.beanName = beanName;
    }
}