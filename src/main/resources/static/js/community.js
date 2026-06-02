/*
 * 모든 게시판 페이지 공통 JS
 * 로그아웃 처리
*/

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('logoutBtn')?.addEventListener('click', async () => {
        const res = await fetch('/api/auth/logout', {method: 'POST'});
        if (res.ok) {
            location.href = '/auth/login';
        } else {
            alert('로그아웃에 실패했습니다.');
        }
    });
});