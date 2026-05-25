package com.itset.itcenteamproject.domain.comment;

import com.itset.itcenteamproject.domain.comment.dto.CommentListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글에 달린 댓글 목록을 조회한다.
     *
     * Board, User를 조인해서 댓글 목록 DTO로 바로 조회한다.
     * 오래된 댓글이 위에 나오도록 작성일 오름차순으로 정렬한다.
     */
    @Query("""
        select new com.itset.itcenteamproject.domain.comment.dto.CommentListItem(
            c.id,
            c.content,
            u.id,
            u.nickname,
            c.createdAt,
            c.updatedAt
        )
        from Comment c
        join c.user u
        where c.board.id = :boardId
        order by c.createdAt asc
    """)
    List<CommentListItem> findCommentsByBoardId(@Param("boardId") Long boardId);
}