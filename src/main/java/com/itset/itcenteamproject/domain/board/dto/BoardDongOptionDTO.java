package com.itset.itcenteamproject.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 드롭다운 옵션 1개(동 코드 + 동 이름)
@Getter
@AllArgsConstructor
public class BoardDongOptionDTO {
    private Integer dongCode;
    private String dongName;
}