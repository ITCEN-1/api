package com.itset.itcenteamproject.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardDetailDTO {
    private Long postId;
    private String title;
    private String content;
    private String writerName;
    private String districtName;
    private String dongName;
    private java.time.LocalDateTime createdAt;
    private Long viewCount;
}

