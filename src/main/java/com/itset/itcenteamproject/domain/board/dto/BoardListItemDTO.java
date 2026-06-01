package com.itset.itcenteamproject.domain.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardListItemDTO {
    private Long postId;
    private String title;
    private String writerName;
    private String districtName;
    private String dongName;
    private java.time.LocalDateTime createdAt;
    private Long viewCount;
}
