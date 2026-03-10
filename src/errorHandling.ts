const STATUS_MESSAGES: Record<number, string> = {
  400: "The request is invalid. Please verify your input and try again.",
  401: "Your session has expired. Please sign in again.",
  403: "You do not have permission to perform this action.",
  404: "The requested resource could not be found.",
  409: "A conflicting resource already exists.",
  422: "Some provided data is invalid. Please review and submit again.",
  429: "Too many requests were sent. Please wait and try again.",
  500: "A server error occurred. Please try again in a moment.",
  503: "The service is temporarily unavailable. Please try again shortly.",
};

type ApiErrorPayload = {
  message?: string;
  error?: string;
  details?: string;
};

export async function getApiErrorMessage(
  response: Response,
  fallback = "Request failed.",
) {
  const contentType = response.headers.get("content-type") ?? "";

  if (contentType.includes("application/json")) {
    const data = (await response.json().catch(() => null)) as ApiErrorPayload | null;
    const backendMessage = data?.message ?? data?.error ?? data?.details;
    if (backendMessage) return backendMessage;
  } else {
    const text = await response.text().catch(() => "");
    if (text.trim()) return text.trim();
  }

  return STATUS_MESSAGES[response.status] ?? `${fallback} (HTTP ${response.status})`;
}

export function getNetworkErrorMessage() {
  return "We could not reach the server. Please check your connection and try again.";
}
