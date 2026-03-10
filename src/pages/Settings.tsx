import { useEffect, useState } from "react";

import DeleteAccountBtn from "../components/DeleteAccountBtn";
import AvatarUpload from "../components/AvatarUpload";
import { getMyAvatar } from "../avatarService";


import "../css/Settings.css";
import bin from "../photos/bin.png";

function Settings() {
    const [open, setOpen] = useState(false);
    const [avatarUrl, setAvatarUrl] = useState<string | null>(null);

    useEffect(() => {
        async function loadAvatar() {
            try {
                const result = await getMyAvatar();
                setAvatarUrl(result.avatarUrl);
            } catch (error) {
                console.error("Load avatar failed:", error);
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
                />
            </div>

            <h1>Settings</h1>

            <ul className="settingsList">
                <li>
                    <a>Delete account</a>

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
        </div>
    );
}

export default Settings;