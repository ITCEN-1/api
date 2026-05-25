package com.itset.itcenteamproject.domain.comment;

import com.itset.itcenteamproject.domain.board.Board;
import com.itset.itcenteamproject.domain.board.BoardRepository;
import com.itset.itcenteamproject.domain.comment.dto.CommentCreateRequest;
import com.itset.itcenteamproject.domain.comment.dto.CommentListItem;
import com.itset.itcenteamproject.domain.comment.dto.CommentUpdateRequest;
import com.itset.itcenteamproject.domain.user.User;
import com.itset.itcenteamproject.domain.user.UserRepository;
import com.itset.itcenteamproject.exception.CustomException;
import com.itset.itcenteamproject.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    //특정 게시글의 댓글 목록을 조회
    @Transactional//(readOnly = true)
    public List<CommentListItem> getComments(Long boardId) {
        return commentRepository.findCommentsByBoardId(boardId);
    }

    //댓글 작성
    @Transactional
    public void createComment(Long userId, Long boardId, CommentCreateRequest req) {
        validateContent(req.getContent());

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(req.getContent().trim())
                .build();

        commentRepository.save(comment);
    }

    //댓글 수정
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest req) {
        validateContent(req.getContent());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        comment.updateContent(req.getContent().trim());
    }

    //댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (!comment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        commentRepository.delete(comment);
    }

    //댓글 본문 검증 메서드
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해야 한다.");
        }
        if (content.length() > 500) {
            throw new IllegalArgumentException("댓글은 500자 이하로 입력해야 한다.");
        }
    }
}
