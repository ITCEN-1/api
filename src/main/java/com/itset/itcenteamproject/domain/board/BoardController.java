package com.itset.itcenteamproject.domain.board;

import com.itset.itcenteamproject.domain.board.dto.*;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import jakarta.servlet.http.HttpSession;
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
    private final SessionUserService sessionUserService;

    @GetMapping("/posts")
    public String getPostList(@RequestParam(required = false) String titleKeyword, @RequestParam(required = false) String district,
                              @RequestParam(required = false) Integer dongCode, @RequestParam(defaultValue = "0") int page, Model model) {
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
        Page<BoardListItemDTO> postPage = boardService.getPosts(c, page);
        model.addAttribute("titleKeyword", titleKeyword);
        model.addAttribute("district", district);
        model.addAttribute("dongCode", dongCode);
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", boardService.getDongsByDistrict(district));
        model.addAttribute("postPage", postPage);
        model.addAttribute("posts", postPage.getContent());
        return "community/post-list";
    }

    @GetMapping("/posts/new")
    public String newPostForm(Model model) {
        model.addAttribute("districts", boardService.getDistricts());
        model.addAttribute("dongs", List.of());
        model.addAttribute("form", new BoardCreateRequest());
        return "community/post-form";
    }

    @GetMapping("/dongs")
    @ResponseBody
    public List<BoardDongOptionDTO> getDongsByDistrict(@RequestParam String district) {
        return boardService.getDongsByDistrict(district);
    }

    @PostMapping("/posts")
    public String createPost(
            @ModelAttribute("form") BoardCreateRequest form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionUserService.getLoginUserId(session);
        Long postId = boardService.createPost(userId, form);

        redirectAttributes.addFlashAttribute("successMessage", "게시글이 작성되었습니다. #" + postId);
        redirectAttributes.addAttribute("district", form.getDistrictName());
        redirectAttributes.addAttribute("dongCode", form.getDongCode());

        return "redirect:/communities/posts";
    }


    @GetMapping("/posts/{postId}")
    public String getPostDetail(@PathVariable Long postId, Model model) {
        model.addAttribute("post", boardService.getPostDetailAndIncreaseView(postId));
        return "community/post-detail";
    }
}
