document.addEventListener("DOMContentLoaded", async () => {
    const container = document.getElementById("recommendation-container");

    try {
        // ✅ 그냥 fetchWithAuth 사용 (토큰 자동 처리 + 리프레시)
        let res = await fetchWithAuth("/api/recommend", {
            method: "GET"
        });

        if (res.status === 401) {
            // 여기까지 오면 refresh도 실패한 상황일 가능성이 큼
            alert("로그인 후 이용 가능한 서비스입니다.");
            window.location.href = "/login";
            return;
        }

        if (!res.ok) {
            container.innerHTML = "<p>추천 뉴스를 불러오는 중 오류가 발생했습니다.</p>";
            return;
        }

        const data = await res.json();  // [{id,title,link,...}, ...]

        if (!data || data.length === 0) {
            container.innerHTML = "<p>추천할 뉴스가 없습니다.</p>";
            return;
        }

        let html = "<ul>";
        data.forEach(news => {
            html += `
                <li style="margin-bottom:10px;">
                    <a href="/news/click?newsId=${news.id}" target="_blank">
                        ${news.title}
                    </a><br/>
                    <small>${news.description ?? ""}</small>
                </li>
            `;
        });
        html += "</ul>";

        container.innerHTML = html;

    } catch (e) {
        console.error(e);
        container.innerHTML = "<p>추천 뉴스를 불러오는 중 오류가 발생했습니다.</p>";
    }
});
