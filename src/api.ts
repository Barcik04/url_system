const API_URL = import.meta.env.VITE_API_URL;


export function setAccessToken(token: string) {
  localStorage.setItem("accessToken", token);
}

export function getAccessToken() {
  return localStorage.getItem("accessToken");
}

export function clearAccessToken() {
  localStorage.removeItem("accessToken");
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

  const doFetch = (accessToken: string | null) => {
    return fetch(`${API_URL}${path}`, {
      ...options,
      credentials: "include", // ważne jeśli cookie
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      },
    });
  };

  let res = await doFetch(token);

  if (res.status === 401) {
    const newToken = await getRefreshedTokenOnce();

    if (!newToken) {
      clearAccessToken();
      return res; 
    }

    res = await doFetch(newToken);
  }

  return res;
}