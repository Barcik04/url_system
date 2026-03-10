import { useState } from "react";

import DeleteAccountBtn from "../components/DeleteAccountBtn";
import AvatarUpload from "../components/AvatarUpload";

import "../css/Settings.css";
import bin from "../photos/bin.png";

function Settings() {
    const [open, setOpen] = useState(false);

    const token = localStorage.getItem("token") || "";

    return (
        <div className="settingsWrap">
             <div className="avatarTopSection">
                <AvatarUpload token={token} />
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