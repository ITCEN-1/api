package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.board.dto.*;
import com.itset.itcenteamproject.domain.comment.CommentRepository;
import com.itset.itcenteamproject.domain.infra.entity.DongLocation;
import com.itset.itcenteamproject.domain.infra.repository.DongLocationRepository;
import com.itset.itcenteamproject.domain.user.*;
import com.itset.itcenteamproject.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final DongLocationRepository dongLocationRepository;
    private final CommentRepository commentRepository;

    //게시글 조회
    @Transactional(readOnly = true)
    public Page<BoardListItemDTO> getPosts(BoardSearchCondition c, int page) {
        return boardRepository.searchPosts(c.getTitleKeyword(), c.getDistrictName(), c.getDongCode(), PageRequest.of(page, 10));
    }

    //내 게시글 조회
    @Transactional(readOnly = true)
    public Page<BoardListItemDTO> getMyPosts(Long userId, BoardSearchCondition c, int page) {
        return boardRepository.searchMyPosts(
                userId,
                c.getTitleKeyword(),
                c.getDistrictName(),
                c.getDongCode(),
                PageRequest.of(page, 10)
        );
    }

    //게시글 작성
    @Transactional
    public Long createPost(Long userId, BoardCreateRequest req) {
        validatePostRequest(req.getDongCode(), req.getTitle(), req.getContent());

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        dongLocationRepository.findById(req.getDongCode()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        Board board = Board.builder().user(user).dongCode(req.getDongCode()).title(req.getTitle().trim()).content(req.getContent().trim()).build();
        return boardRepository.save(board).getId();
    }

    // 게시글 수정 화면에 기존 게시글 값을 채워 넣기 위한 메서드
    @Transactional(readOnly = true)
    public BoardUpdateRequest getPostForEdit(Long userId, Long postId) {
        // 수정할 게시글을 조회한다.
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        // 현재 로그인한 사용자와 게시글 작성자가 다르면 수정할 수 없다.
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 게시글에 저장된 dongCode로 동 정보를 조회한다.
        DongLocation dong = dongLocationRepository.findById(board.getDongCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        // 수정 화면에 채워 넣을 form DTO를 만든다.
        BoardUpdateRequest form = new BoardUpdateRequest();
        form.setDistrictName(dong.getDistrictName());
        form.setDongCode(dong.getDongCode());
        form.setTitle(board.getTitle());
        form.setContent(board.getContent());

        return form;
    }

    //수정 요청을 받아 실제 게시글 값을 변경
    @Transactional
    public void updatePost(Long userId, Long postId, BoardUpdateRequest req) {
        validatePostRequest(req.getDongCode(), req.getTitle(), req.getContent());

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        dongLocationRepository.findById(req.getDongCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        board.updatePost(
                req.getDongCode(),
                req.getTitle().trim(),
                req.getContent().trim()
        );
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long userId, Long postId) {
        // 1) 삭제할 게시글 조회
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        // 2) 작성자 본인인지 확인
        // board.getUser().getId() = 게시글 작성자 ID
        // userId = 현재 로그인한 사용자 ID
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        // 3) 게시글 삭제
        commentRepository.deleteByBoardId(postId);
        boardRepository.delete(board);
    }

    // 조회수 증가 (본인 게시글 조회 수 제한)
    @Transactional
    public BoardDetailDTO getPostDetailAndIncreaseView(Long postId, Long loginUserId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        boolean isWriter = loginUserId != null && board.getUser().getId().equals(loginUserId);

        if (!isWriter) {
            board.increaseViewCount();
        }
        return boardRepository.findDetailById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    // 게시글 검색/작성 화면에서 사용할 구 목록을 조회
    @Transactional(readOnly = true)
    public List<String> getDistricts() {
        return dongLocationRepository.findAllDistrictNames();
    }

    @Transactional(readOnly = true)
    public List<BoardDongOptionDTO> getDongsByDistrict(String district) {
        if (district == null || district.isBlank()) return List.of();
        return dongLocationRepository.findAllByDistrictName(district.trim()).stream()
                .sorted(Comparator.comparing(DongLocation::getDongName))
                .map(d -> new BoardDongOptionDTO(d.getDongCode(), d.getDongName())).toList();
    }

    private void validatePostRequest(Integer dongCode, String title, String content) {
        if (dongCode == null) {
            throw new CustomException(ErrorCode.REQUIRED_DONG_CODE);
        }
        if (title == null || title.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_TITLE);
        }
        if (content == null || content.isBlank()) {
            throw new CustomException(ErrorCode.REQUIRED_CONTENT);
        }
    }
}
