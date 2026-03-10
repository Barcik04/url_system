const API_URL = import.meta.env.VITE_API_URL;


let accessToken: string | null = null;

export function setAccessToken(t: string | null) {
  accessToken = t;
  if (t) localStorage.setItem("accessToken", t);
  else localStorage.removeItem("accessToken");
}

export function clearAccessToken() {
  accessToken = null;
  localStorage.removeItem("accessToken");
}

export function getAccessToken() {
  return accessToken ?? localStorage.getItem("accessToken");
}



async function refreshAccessToken(): Promise<string | null> {
  const res = await fetch(`${API_URL}/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
  });

  if (!res.ok) return null;

  const data = (await res.json()) as { accessToken: string };
  setAccessToken(data.accessToken);
  return data.accessToken;
}



let refreshPromise: Promise<string | null> | null = null;



async function getRefreshedTokenOnce(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = refreshAccessToken().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}




export async function apiFetch(path: string, options: RequestInit = {}) {
  const token = getAccessToken();

  const normalizedHeaders = new Headers(options.headers ?? undefined);
    if (!normalizedHeaders.has("Content-Type") && !(options.body instanceof FormData)) {
      normalizedHeaders.set("Content-Type", "application/json");
    }

    const doFetch = (accessToken: string | null, overrideAuthorization = false) => {
      const headers = new Headers(normalizedHeaders);
      if (accessToken && (overrideAuthorization || !headers.has("Authorization"))) {
        headers.set("Authorization", `Bearer ${accessToken}`);
      }

    return fetch(`${API_URL}${path}`, {
      ...options,
      credentials: "include",
      headers,
    });
  }

  let res = await doFetch(token);

  if (res.status === 401 || res.status === 403) {
        const newToken = await getRefreshedTokenOnce();

    if (!newToken) {
      clearAccessToken();
      return res; 
    }

    res = await doFetch(newToken, true);
    }

  return res;
}