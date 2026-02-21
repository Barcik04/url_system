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

export async function apiFetch(path: string, options: RequestInit = {}) {
  const token = getAccessToken();

  const res = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (res.status === 401) {
    clearAccessToken();
  }

  return res;
}