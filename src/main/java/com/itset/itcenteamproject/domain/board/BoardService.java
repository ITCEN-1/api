package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.board.dto.*;
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

    @Transactional(readOnly = true)
    public Page<BoardListItemDTO> getPosts(BoardSearchCondition c, int page) {
        return boardRepository.searchPosts(c.getTitleKeyword(), c.getDistrictName(), c.getDongCode(), PageRequest.of(page, 10));
    }

    @Transactional
    public Long createPost(Long userId, BoardCreateRequest req) {
        if (req.getDongCode() == null) throw new IllegalArgumentException("동을 선택해주세요.");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (req.getContent() == null || req.getContent().isBlank()) throw new IllegalArgumentException("본문을 입력해주세요.");

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        dongLocationRepository.findById(req.getDongCode()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

        Board board = Board.builder().user(user).dongCode(req.getDongCode()).title(req.getTitle().trim()).content(req.getContent().trim()).build();
        return boardRepository.save(board).getId();
    }

    //수정 화면에 기존 글 값을 채워 넣기 위해
    @Transactional(readOnly = true)
    public BoardUpdateRequest getPostForEdit(Long userId, Long postId) {
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        DongLocation dong = dongLocationRepository.findById(board.getDongCode())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_DONG_CODE));

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
        if (req.getDongCode() == null) throw new IllegalArgumentException("동을 선택해주세요.");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw new IllegalArgumentException("제목을 입력해주세요.");
        if (req.getContent() == null || req.getContent().isBlank()) throw new IllegalArgumentException("본문을 입력해주세요.");

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

    @Transactional
    public BoardDetailDTO getPostDetailAndIncreaseView(Long postId) {
        Board board = boardRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        board.increaseViewCount();
        return boardRepository.findDetailById(postId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    @Transactional(readOnly = true)
    public List<String> getDistricts() {
        return dongLocationRepository.findAll().stream().map(DongLocation::getDistrictName).distinct().sorted().toList();
    }

    @Transactional(readOnly = true)
    public List<BoardDongOptionDTO> getDongsByDistrict(String district) {
        if (district == null || district.isBlank()) return List.of();
        return dongLocationRepository.findAllByDistrictName(district.trim()).stream()
                .sorted(Comparator.comparing(DongLocation::getDongName))
                .map(d -> new BoardDongOptionDTO(d.getDongCode(), d.getDongName())).toList();
    }
}
