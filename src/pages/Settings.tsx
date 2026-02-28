import { useEffect, useState } from "react"

import DeleteAccountBtn from "../components/DeleteAccountBtn"

import "../css/Settings.css"
import bin from "../photos/bin.png"


function Settings() {
    const [open, setOpen] = useState(false);

    return (
    <div className="settingsWrap">
        <h1>Settings</h1>

        <ul className="settingsList">
        <li>
            <a>Delete account</a>
            <button className="deleteAccountBtn" onClick={() => setOpen(true)}>
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