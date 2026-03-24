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
    const [isAvatarPreviewOpen, setIsAvatarPreviewOpen] = useState(false);

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

    return (
        <div className="settingsWrap">
            <div className="avatarTopSection">
                <AvatarUpload
                    currentAvatarUrl={avatarUrl}
                    onAvatarUpdated={setAvatarUrl}
                    onPreviewClick={() => avatarUrl && setIsAvatarPreviewOpen(true)}
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

            {isAvatarPreviewOpen && avatarUrl && (
                <div
                    className="avatarPreviewOverlay"
                    onClick={() => setIsAvatarPreviewOpen(false)}
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