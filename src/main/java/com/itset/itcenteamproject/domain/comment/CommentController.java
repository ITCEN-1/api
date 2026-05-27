package com.itset.itcenteamproject.domain.comment;

import com.itset.itcenteamproject.domain.comment.dto.CommentCreateRequest;
import com.itset.itcenteamproject.domain.comment.dto.CommentUpdateRequest;
import com.itset.itcenteamproject.domain.user.service.SessionUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/communities/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final SessionUserService sessionUserService;

    //댓글 작성
    @PostMapping
    public String createComment(
            @PathVariable Long postId,
            @ModelAttribute CommentCreateRequest form,
            HttpSession session,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionUserService.getLoginUserId(session);

        commentService.createComment(userId, postId, form);

        if (isAjax(request)) {
            return renderCommentSection(postId, userId, model);
        }

        redirectAttributes.addFlashAttribute("successMessage", "댓글이 작성되었습니다.");
        return "redirect:/communities/posts/" + postId;
    }

    //댓글 수정
    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @ModelAttribute CommentUpdateRequest form,
            HttpSession session,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionUserService.getLoginUserId(session);

        commentService.updateComment(userId, commentId, form);

        if (isAjax(request)) {
            return renderCommentSection(postId, userId, model);
        }

        redirectAttributes.addFlashAttribute("successMessage", "댓글이 수정되었습니다.");
        return "redirect:/communities/posts/" + postId;
    }

    //댓글 삭제
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            HttpSession session,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = sessionUserService.getLoginUserId(session);

        commentService.deleteComment(userId, commentId);

        if (isAjax(request)) {
            return renderCommentSection(postId, userId, model);
        }

        redirectAttributes.addFlashAttribute("successMessage", "댓글이 삭제되었습니다.");
        return "redirect:/communities/posts/" + postId;
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    private String renderCommentSection(Long postId, Long userId, Model model) {
        model.addAttribute("comments", commentService.getComments(postId));
        model.addAttribute("loginUserId", userId);
        model.addAttribute("postId", postId);

        return "community/post-detail :: commentSection";
    }
}
