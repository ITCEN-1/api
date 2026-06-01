package com.itset.itcenteamproject.domain.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 게시글 작성 폼 데이터 바인딩 DTO
@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest {

    // 사용자가 선택한 구 이름 (폼 상태 유지/리다이렉트 파라미터용)
    private String districtName;

    // 사용자가 선택한 동 코드 (실제 저장에 사용)
    private Integer dongCode;

    // 게시글 제목
    private String title;

    // 게시글 본문
    private String content;
}
