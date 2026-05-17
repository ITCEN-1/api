package com.itset.itcenteamproject.domain.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardUpdateRequest {
    private String districtName;
    private Integer dongCode;
    private String title;
    private String content;
}