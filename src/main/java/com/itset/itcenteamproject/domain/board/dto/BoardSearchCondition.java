package com.itset.itcenteamproject.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardSearchCondition {
    private String titleKeyword;
    private String districtName;
    private Integer dongCode;
}
