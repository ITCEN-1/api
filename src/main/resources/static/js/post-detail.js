/*
 * 상세 페이지 전용 JS
 * 댓글 작성/수정/삭제 AJAX, 댓글 수정 UI 토글
*/

document.addEventListener('DOMContentLoaded', () => {
    document.addEventListener('submit', async (event) => {
        const form = event.target;

        const isCommentCreate = form.classList.contains('comment-form');
        const isCommentEdit = form.classList.contains('comment-edit-form');
        const isCommentDelete = form.classList.contains('comment-delete-form');

        if (!isCommentCreate && !isCommentEdit && !isCommentDelete) {
            return;
        }

        event.preventDefault();

        if (isCommentDelete && !confirm('댓글을 삭제하시겠습니까?')) {
            return;
        }

        const formData = new FormData(form);

        const response = await fetch(form.action, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: formData
        });

        if (!response.ok) {
            alert('댓글 처리에 실패했습니다.');
            return;
        }

        const html = await response.text();
        document.getElementById('commentSection').outerHTML = html;
    });

    document.addEventListener('click', (event) => {
        const editButton = event.target.closest('.comment-edit-btn');
        const cancelButton = event.target.closest('.comment-edit-cancel-btn');

        if (editButton) {
            const commentId = editButton.dataset.commentId;
            const commentItem = editButton.closest('.comment-item');
            const viewArea = commentItem.querySelector('.comment-view-area');
            const editArea = document.getElementById(`commentEditArea-${commentId}`);

            viewArea.style.display = 'none';
            editArea.style.display = 'block';
            return;
        }

        if (cancelButton) {
            const commentId = cancelButton.dataset.commentId;
            const commentItem = cancelButton.closest('.comment-item');
            const viewArea = commentItem.querySelector('.comment-view-area');
            const editArea = document.getElementById(`commentEditArea-${commentId}`);

            editArea.style.display = 'none';
            viewArea.style.display = 'block';
        }
    });
});