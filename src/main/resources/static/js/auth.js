// ✅ Access 만료 시 자동 Refresh 포함 fetch
async function fetchWithAuth(url, options = {}) {
    let accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (!options.headers) options.headers = {};

    // 🔥 1) 토큰 형식 검증 (aaa.bbb.ccc 같이 .이 2개 있어야 정상 JWT)
    const isValidFormat = accessToken && accessToken.split('.').length === 3;

    if (!isValidFormat) {
        if (accessToken) {
            console.warn("[fetchWithAuth] 잘못된 AccessToken 형식, localStorage에서 제거");
        }
        localStorage.removeItem("accessToken");
        accessToken = null;
    }

    // 🔥 2) 형식이 정상일 때만 Authorization 헤더에 추가
    if (accessToken) {
        options.headers["Authorization"] = "Bearer " + accessToken;
    }

    console.log("[fetchWithAuth] 요청:", url, "options=", options);

    let res = await fetch(url, options);
    console.log("[fetchWithAuth] 응답 status =", res.status, "for", url);

    // 401 → refresh 시도
    if (res.status === 401 && refreshToken) {
        console.log("[fetchWithAuth] Access 만료 → Refresh 시도");

        let refreshRes = await fetch("/api/auth/refresh", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken })
        });

        console.log("[fetchWithAuth] /api/auth/refresh status =", refreshRes.status);

        if (refreshRes.status === 200) {
            let data = await refreshRes.json();
            console.log("[fetchWithAuth] Refresh 성공 → 새 Access 발급");
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);

            options.headers["Authorization"] = "Bearer " + data.accessToken;
            res = await fetch(url, options);
            console.log("[fetchWithAuth] 새 Access로 재요청 status =", res.status);
        } else {
            console.warn("[fetchWithAuth] Refresh 실패 → 로그아웃");
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            window.location.href = "/login";
        }
    }

    return res;
}

// ✅ 로그인 상태 체크
async function checkLogin() {
    console.log("[checkLogin] 호출됨");

    const loginInfo = document.getElementById("login-info");
    const logoutBtn = document.getElementById("logoutBtn");

    if (!loginInfo || !logoutBtn) {
        console.warn("[checkLogin] login-info 혹은 logoutBtn 요소 없음");
        return;
    }

    try {
        let res = await fetchWithAuth("/api/me");

        if (res.ok) {  // 200~299
            const text = await res.text();
            console.log("[checkLogin] /api/me OK, text =", text);
            loginInfo.innerText = text;
            logoutBtn.style.display = "inline";
        } else {
            console.log("[checkLogin] /api/me not OK, status =", res.status);
            loginInfo.innerText = "(로그인 안됨)";
            logoutBtn.style.display = "none";
        }
    } catch (e) {
        console.error("[checkLogin] fetch 중 에러:", e);
        loginInfo.innerText = "(로그인 안됨)";
        logoutBtn.style.display = "none";
    }
}

// ✅ 로그아웃
async function logout() {
    console.log("[logout] 호출됨");

    const accessToken = localStorage.getItem("accessToken");

    // 🔥 토큰 형식 체크 (JWT 아니면 서버에 쏘지 말고 클라이언트만 정리)
    if (!accessToken || accessToken.split('.').length !== 3) {
        console.warn("[logout] 유효한 AccessToken 없음, 클라이언트만 정리");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/";
        return;
    }

    try {
        const res = await fetch("/api/auth/logout", {
            method: "POST",
            headers: { "Authorization": "Bearer " + accessToken }
        });
        console.log("[logout] /api/auth/logout status =", res.status);
    } catch (e) {
        console.error("[logout] /api/auth/logout 호출 중 에러:", e);
        // 어차피 토큰 지우면 되니까 에러여도 그냥 진행
    }

    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    console.log("[logout] 로그아웃 완료 → 메인으로 이동");
    window.location.href = "/";
}

// 🔥 전역(window)에 명시적으로 붙여주기 (onclick으로 쓰기 위함)
window.checkLogin = checkLogin;
window.logout = logout;

// 🔥 load 시점에 확실하게 한 번 호출
window.addEventListener("load", () => {
    console.log("[global] window.load → checkLogin 호출");
    checkLogin();
});
