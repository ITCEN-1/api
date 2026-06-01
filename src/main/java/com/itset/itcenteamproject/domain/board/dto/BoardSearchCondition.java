package com.itset.itcenteamproject.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

//검색 조건
public class BoardSearchCondition {
    private String titleKeyword;
    private String districtName;
    private Integer dongCode;
}
