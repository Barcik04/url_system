import { useEffect, useRef, useState } from "react";
import {
    presignAvatarUpload,
    uploadFileToPresignedUrl,
    confirmAvatarUpload,
    getMyAvatar,
} from "../avatarService";

type AvatarUploadProps = {
    currentAvatarUrl?: string | null;
    onAvatarUpdated?: (newAvatarUrl: string | null) => void;
};

function AvatarUpload({
    currentAvatarUrl,
    onAvatarUpdated,
}: AvatarUploadProps) {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(currentAvatarUrl ?? null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const inputRef = useRef<HTMLInputElement | null>(null);
    console.log("AvatarUpload render");

    useEffect(() => {
        setPreviewUrl(currentAvatarUrl ?? null);
    }, [currentAvatarUrl]);


    function handleOpenFilePicker() {
        inputRef.current?.click();
    }

    function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
        const file = event.target.files?.[0] ?? null;

        setError("");

        if (!file) {
            return;
        }

        if (!file.type.startsWith("image/")) {
            setError("Please select an image file.");
            return;
        }

        setSelectedFile(file);
        setPreviewUrl(URL.createObjectURL(file));
    }

    async function handleUpload() {
        if (!selectedFile) {
            setError("Choose an image first.");
            return;
        }

        try {
            setLoading(true);
            setError("");

            const presignResponse = await presignAvatarUpload(selectedFile);
            await uploadFileToPresignedUrl(presignResponse.uploadUrl, selectedFile);
            const confirmedAvatar = await confirmAvatarUpload(presignResponse.key);
            setPreviewUrl(confirmedAvatar.avatarUrl ?? null);

            if (onAvatarUpdated) {
                onAvatarUpdated(confirmedAvatar.avatarUrl ?? null);
            }

            setSelectedFile(null);

            if (inputRef.current) {
                inputRef.current.value = "";
            }
        } catch (err) {
            if (err instanceof Error) {
                setError(err.message);
            } else {
                setError("Avatar upload failed.");
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="avatarUploadWrap">
            <div className="avatarBox" onClick={handleOpenFilePicker}>
                {previewUrl ? (
                    <img src={previewUrl} alt="User avatar" className="avatarImage" />
                ) : (
                    <div className="avatarPlaceholder">No avatar</div>
                )}

                <div className="avatarOverlay">
                    <button
                        type="button"
                        className="editAvatarBtn"
                        onClick={(e) => {
                            e.stopPropagation();
                            handleOpenFilePicker();
                        }}
                    >
                        Edit avatar
                    </button>
                </div>
            </div>

            <input
                ref={inputRef}
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="hiddenAvatarInput"
            />

            {selectedFile && (
                <button
                    type="button"
                    className="saveAvatarBtn"
                    onClick={handleUpload}
                    disabled={loading}
                >
                    {loading ? "Uploading..." : "Save avatar"}
                </button>
            )}

            {error && <p className="avatarError">{error}</p>}
        </div>
    );
}

export default AvatarUpload;