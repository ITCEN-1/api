package com.itset.itcenteamproject.domain.comment.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentListItem {

    private Long commentId;
    private String content;
    private Long writerId;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
