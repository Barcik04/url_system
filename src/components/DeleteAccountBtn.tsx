import { useState } from "react";
import { apiFetch } from "../api";

import "../css/Settings.css"




export default function DeleteAccountBtn() {
  
    const [msg, setMsg] = useState("")
    const [password, setPassword] = useState("");

    async function handleDelete(e: React.FormEvent) {
        e.preventDefault()

        try {
            const res = await apiFetch("/api/v1/delete-account", {
                method: "DELETE",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ password }),
            });

            if (!res.ok) {
                const txt = await res.text().catch(() => "");
                throw new Error(txt || `HTTP ${res.status}`);
            }

            setMsg("Deletion Success")
            console.log("sucess")
            } catch (e: any) {
              setMsg("error")
              console.log(e)
            }
    }

    return (
        <form className="deleteAccountForm" onSubmit={handleDelete}>
            <h1>Delete Account</h1>
            <input className="password" value={password} placeholder="Insert your password" onChange={(e) => setPassword(e.target.value)}/>
            <button className="confirmDeleteAccountBtn" type="submit" disabled={!password}>Confirm Delete</button>
        </form>
    )
}
