// ✅ Access 만료 시 자동 Refresh 포함 fetch
async function fetchWithAuth(url, options = {}) {
    let accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (!options.headers) options.headers = {};
    if (accessToken) {
        options.headers["Authorization"] = "Bearer " + accessToken;
    }

    let res = await fetch(url, options);

    // ✅ Access 만료 → Refresh 시도
    if (res.status === 401 && refreshToken) {
        console.log("[fetchWithAuth] Access 만료 → Refresh 시도");

        let refreshRes = await fetch("/api/auth/refresh", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken })
        });

        if (refreshRes.status === 200) {
            let data = await refreshRes.json();
            console.log("[fetchWithAuth] Refresh 성공 → 새 Access 발급");
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);

            // 새 Access로 재요청
            options.headers["Authorization"] = "Bearer " + data.accessToken;
            res = await fetch(url, options);
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
    const loginInfo = document.getElementById("login-info");
    const logoutBtn = document.getElementById("logoutBtn");

    let res = await fetchWithAuth("/api/me");

    if (res.status === 200) {
        const text = await res.text();
        loginInfo.innerText = text;
        logoutBtn.style.display = "inline";
    } else {
        loginInfo.innerText = "(로그인 안됨)";
        logoutBtn.style.display = "none";
    }
}

// ✅ 로그아웃
async function logout() {
    const accessToken = localStorage.getItem("accessToken");
    await fetch("/api/auth/logout", {
        method: "POST",
        headers: { "Authorization": "Bearer " + accessToken }
    });
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    console.log("[logout] 로그아웃 완료");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", checkLogin);
