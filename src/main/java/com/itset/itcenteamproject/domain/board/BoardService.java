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
