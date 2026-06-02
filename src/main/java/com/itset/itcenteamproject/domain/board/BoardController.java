package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.board.dto.*;
import com.itset.itcenteamproject.domain.comment.CommentService;
import com.itset.itcenteamproject.domain.comment.dto.CommentListItem;
import com.itset.itcenteamproject.global.component.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/communities")
public class BoardController {
    private final BoardService boardService;
    private final SessionManager sessionManager;
    private final CommentService commentService;

    // 게시글 조회, 검색
    @GetMapping("/posts")
    public String getPostList(@RequestParam(required = false) String titleKeyword, @RequestParam(required = false) String district,
                              @RequestParam(required = false) Integer dongCode, @RequestParam(defaultValue = "0") int page, Model model) {
        BoardSearchCondition c = createSearchCondition(titleKeyword, district, dongCode);
        Page<BoardListItemDTO> postPage = boardService.getPosts(c, page);
        addPostListModel(model, titleKeyword, district, dongCode, postPage, false, "게시글 목록", "/communities/posts");
        return "community/post-list";
    }

    // 내 게시글 조회
    @GetMapping("/my/posts")
    public String getMyPostList(@RequestParam(required = false) String titleKeyword,
                                @RequestParam(required = false) String district,
                                @RequestParam(required = false) Integer dongCode,
                                @RequestParam(defaultValue = "0") int page,
                                Model model) {
        Long userId = sessionManager.getLoginUserId();

        BoardSearchCondition c = createSearchCondition(titleKeyword, district, dongCode);
        Page<BoardListItemDTO> postPage = boardService.getMyPosts(userId, c, page);
        addPostListModel(model, titleKeyword, district, dongCode, postPage, true, "내가 쓴 게시글", "/communities/my/posts");

        return "community/post-list";
    }

    @GetMapping("/posts/new")
    public String newPostForm(Model model) {
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", List.of());
        model.addAttribute("form", new BoardCreateRequest());
        return "community/post-form";
    }

    // 동 추출
    @GetMapping("/dongs")
    @ResponseBody
    public List<BoardDongOptionDTO> getDongsByDistrict(@RequestParam String district) {
        return boardService.getDongsByDistrict(district);
    }

    // 게시글 등록
    @PostMapping("/posts")
    public String createPost(
            @ModelAttribute("form") BoardCreateRequest form,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionManager.getLoginUserId();
        Long postId = boardService.createPost(userId, form);

        redirectAttributes.addFlashAttribute("successMessage", "게시글이 작성되었습니다.");
        redirectAttributes.addAttribute("district", form.getDistrictName());
        redirectAttributes.addAttribute("dongCode", form.getDongCode());

        return "redirect:/communities/posts";
    }


    // 게시글 상세보기 + 댓글
    @GetMapping("/posts/{postId}")
    public String getPostDetail(
            @PathVariable Long postId,
            Model model
    ) {
        Long loginUserId = sessionManager.getLoginUserId();
        BoardDetailDTO post = boardService.getPostDetailAndIncreaseView(postId, loginUserId);

        boolean editable = loginUserId != null && loginUserId.equals(post.getWriterId());

        List<CommentListItem> comments = commentService.getComments(postId);

        model.addAttribute("post", post);
        model.addAttribute("editable", editable);
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", boardService.getDongsByDistrict(post.getDistrictName()));

        model.addAttribute("comments", comments);
        model.addAttribute("loginUserId", loginUserId);

        model.addAttribute("postId", postId);

        return "community/post-detail";
    }

    // 게시글 수정 화면 보여줌
    @GetMapping("/posts/{postId}/edit")
    public String editPostForm(
            @PathVariable Long postId,
            Model model
    ) {
        Long userId = sessionManager.getLoginUserId();
        BoardUpdateRequest form = boardService.getPostForEdit(userId, postId);
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", boardService.getDongsByDistrict(form.getDistrictName()));
        model.addAttribute("form", form);
        model.addAttribute("postId", postId);

        return "community/post-edit";
    }

    //수정 저장
    @PostMapping("/posts/{postId}/edit")
    public String updatePost(
            @PathVariable Long postId,
            @ModelAttribute("form") BoardUpdateRequest form,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionManager.getLoginUserId();

        boardService.updatePost(userId, postId, form);

        redirectAttributes.addFlashAttribute("successMessage", "게시글이 수정되었습니다.");
        return "redirect:/communities/posts/" + postId;
    }


    //게시글 삭제
    @PostMapping("/posts/{postId}/delete")
    public String deletePost(
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        // 1) 현재 로그인한 사용자 ID 조회
        // 로그인하지 않은 사용자는 sessionManager SESSION_EXPIRED 예외 발생
        Long userId = sessionManager.getLoginUserId();

        // 2) 삭제 처리
        // 실제 권한 검사는 service에서 다시 수행
        boardService.deletePost(userId, postId);

        // 3) 삭제 후 목록으로 이동
        redirectAttributes.addFlashAttribute("successMessage", "게시글이 삭제되었습니다.");
        return "redirect:/communities/posts";
    }

    //전체 게시글 검색, 내 게시글 검색 중복 메서드
    private BoardSearchCondition createSearchCondition(String titleKeyword, String district, Integer dongCode) {
        BoardSearchCondition c = new BoardSearchCondition();
        c.setTitleKeyword(titleKeyword);

        // 검색 규칙:
        // 1) 동 선택 시: 동 기준 검색(구도 함께 전달 가능)
        // 2) 동 미선택 + 구 선택 시: 구 기준 검색
        // 3) 둘 다 미선택: 전체 검색
        if (dongCode != null) {
            c.setDongCode(dongCode);
            c.setDistrictName(district);
        } else if (district != null && !district.isBlank()) {
            c.setDistrictName(district);
            c.setDongCode(null);
        } else {
            c.setDistrictName(null);
            c.setDongCode(null);
        }

        return c;
    }

    private void addPostListModel(
            Model model,
            String titleKeyword,
            String district,
            Integer dongCode,
            Page<BoardListItemDTO> postPage,
            boolean isMyPosts,
            String pageTitle,
            String listUrl
    ) {
        List<BoardDongOptionDTO> dongs = boardService.getDongsByDistrict(district);

        String dongName = null;
        if (dongCode != null) {
            dongName = dongs.stream()
                    .filter(d -> d.getDongCode().equals(dongCode))
                    .map(BoardDongOptionDTO::getDongName)
                    .findFirst()
                    .orElse(null);
        }

        model.addAttribute("titleKeyword", titleKeyword);
        model.addAttribute("district", district);
        model.addAttribute("dongCode", dongCode);
        model.addAttribute("dongName", dongName);
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", boardService.getDongsByDistrict(district));
        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("isMyPosts", isMyPosts);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("listUrl", listUrl);
    }
}
