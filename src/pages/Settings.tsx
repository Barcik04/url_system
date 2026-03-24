import { useEffect, useState } from "react";
import DeleteAccountBtn from "../components/DeleteAccountBtn";
import AvatarUpload from "../components/AvatarUpload";
import { getMyAvatar } from "../avatarService";
import { Link } from "react-router-dom";
import "../css/Settings.css";
import bin from "../photos/bin.png";

function Settings() {
    const [open, setOpen] = useState(false);
    const [avatarUrl, setAvatarUrl] = useState<string | null>(null);
    const [msg, setMsg] = useState("");
    const [msgType, setMsgType] = useState<"success" | "error" | "">("");
    const [isAvatarPreviewVisible, setIsAvatarPreviewVisible] = useState(false);
    const [isAvatarPreviewClosing, setIsAvatarPreviewClosing] = useState(false);

    useEffect(() => {
        async function loadAvatar() {
            try {
                const result = await getMyAvatar();
                setAvatarUrl(result.avatarUrl);
                setMsg("");
                setMsgType("");
            } catch (error) {
                console.error("Load avatar failed:", error);
                setMsg("Unable to load avatar.");
                setMsgType("error");
            }
        }

        loadAvatar();
    }, []);

    function openAvatarPreview() {
        if (!avatarUrl) return;
        setIsAvatarPreviewClosing(false);
        setIsAvatarPreviewVisible(true);
    }

    function closeAvatarPreview() {
        setIsAvatarPreviewClosing(true);

        setTimeout(() => {
            setIsAvatarPreviewVisible(false);
            setIsAvatarPreviewClosing(false);
        }, 300);
    }

    return (
        <div className="settingsWrap">
            <div className="avatarTopSection">
                <AvatarUpload
                    currentAvatarUrl={avatarUrl}
                    onAvatarUpdated={setAvatarUrl}
                    onPreviewClick={openAvatarPreview}
                />
            </div>

            {msg && <p className={`settings-msg ${msgType}`}>{msg}</p>}

            <h1>Settings</h1>

            <ul className="settingsList">
                <li className="settingsItem">
                    <span className="settingsLabel">Subscriptions</span>
                    <Link to="/subscriptions" className="subscriptionsLink">
                        Open
                    </Link>
                </li>

                <li className="settingsItem">
                    <span className="settingsLabel">Delete account</span>
                    <button
                        className="deleteAccountBtn"
                        onClick={() => setOpen(true)}
                    >
                        <img src={bin} alt="bin" />
                    </button>
                </li>
            </ul>

            {open && (
                <div className="overlay" onClick={() => setOpen(false)}>
                    <div onClick={(e) => e.stopPropagation()}>
                        <DeleteAccountBtn />
                    </div>
                </div>
            )}

            {isAvatarPreviewVisible && avatarUrl && (
                <div
                    className={`avatarPreviewOverlay ${isAvatarPreviewClosing ? "closing" : "open"}`}
                    onClick={closeAvatarPreview}
                >
                    <div
                        className="avatarPreviewContent"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <img
                            src={avatarUrl}
                            alt="Avatar preview"
                            className="avatarPreviewImage"
                        />
                    </div>
                </div>
            )}
        </div>
    );
}

export default Settings;