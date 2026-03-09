const API_BASE_URL = import.meta.env.VITE_API_URL;

export type PresignAvatarUploadRequest = {
  contentType: string;
};

export type PresignAvatarUploadResponse = {
  uploadUrl: string;
  key: string;
};

export type ConfirmAvatarUploadRequest = {
  key: string;
};

export type UserAvatarResponse = {
  avatarUrl: string | null;
};

export async function presignAvatarUpload(
  token: string,
  contentType: string
): Promise<PresignAvatarUploadResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/users/me/avatar/presign`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ contentType }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Presign failed: ${errorText}`);
  }

  return response.json();
}

export async function uploadFileToPresignedUrl(
  uploadUrl: string,
  file: File
): Promise<void> {
  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: {
      "Content-Type": file.type,
    },
    body: file,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`S3 upload failed: ${errorText}`);
  }
}

export async function confirmAvatarUpload(
  token: string,
  key: string
): Promise<UserAvatarResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/users/me/avatar`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ key }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Confirm failed: ${errorText}`);
  }

  return response.json();
}