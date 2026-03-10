import { apiFetch } from "./api";

export type PresignAvatarUploadResponse = {
  uploadUrl: string;
  key: string;
};

export type UserAvatarResponse = {
  avatarUrl: string | null;
};

function resolveContentType(file: File): string {
  if (!file) {
    throw new Error("File is undefined");
  }

  if (file.type && file.type.trim() !== "") {
    return file.type;
  }

  const name = file.name ? file.name.toLowerCase() : "";

  if (name.endsWith(".png")) return "image/png";
  if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
  if (name.endsWith(".webp")) return "image/webp";
  if (name.endsWith(".gif")) return "image/gif";

  return "application/octet-stream";
}

export async function presignAvatarUpload(
  file: File
): Promise<PresignAvatarUploadResponse> {

  const contentType = resolveContentType(file);

  const response = await apiFetch(`/api/v1/users/me/avatar/presign`, {
    method: "POST",
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

  const contentType = resolveContentType(file);

  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: {
      "Content-Type": contentType,
    },
    body: file,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`S3 upload failed: ${errorText}`);
  }
}

export async function confirmAvatarUpload(
  key: string
): Promise<UserAvatarResponse> {

  const response = await apiFetch(`/api/v1/users/me/avatar`, {
    method: "PUT",
    body: JSON.stringify({ key }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Confirm failed: ${errorText}`);
  }

  return response.json();
}

export async function uploadAvatar(
  file: File
): Promise<UserAvatarResponse> {

  if (!file) {
    throw new Error("No file selected");
  }

  const presign = await presignAvatarUpload(file);

  await uploadFileToPresignedUrl(presign.uploadUrl, file);

  return confirmAvatarUpload(presign.key);
}