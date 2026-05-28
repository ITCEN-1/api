/*
 * 목록/작성/수정 공통 JS
 * 구 선택하면 동 목록 불러오기
*/
document.addEventListener('DOMContentLoaded', () => {

    const districtSelect = document.getElementById('districtSelect');
    const dongSelect = document.getElementById('dongSelect');

    // 상세 페이지처럼 select가 없는 페이지에 들어가도 에러 안 나게 막음
    if (!districtSelect || !dongSelect) {
        return;
    }

    function resetDongOptions() {
        dongSelect.innerHTML = '<option value="">동을 선택하세요</option>';
    }

    if (districtSelect && dongSelect) {
        districtSelect.addEventListener('change', async () => {
            resetDongOptions();

            const district = districtSelect.value;

            if (!district) {
                return;
            }

            try {
                // 사용자가 구를 선택했을 때
                // 선택한 구 이름을 서버에 보내 동 목록을 받아옴
                const response = await fetch(`/communities/dongs?district=${encodeURIComponent(district)}`);

                if (!response.ok) {
                    alert('동 목록을 불러오지 못했습니다.');
                    return;
                }

                const body = await response.json();
                const dongs = Array.isArray(body) ? body : body.content;

                if (!Array.isArray(dongs)) {
                    alert('동 목록 응답 형식이 올바르지 않습니다.');
                    return;
                }

                dongs.forEach((dong) => {
                    const option = document.createElement('option');
                    option.value = dong.dongCode;
                    option.textContent = dong.dongName;
                    dongSelect.appendChild(option);
                });
            } catch (error) {
                console.error(error);
                alert('동 목록을 불러오지 못했습니다.');
            }
        });
    }
})
