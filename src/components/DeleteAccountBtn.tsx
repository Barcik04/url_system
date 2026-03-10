import { useState } from "react";
import { apiFetch } from "../api";
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling";

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
                setMsg(await getApiErrorMessage(res, "Failed to delete account."));
                return;
            }

            setMsg("Account deleted successfully.")
            console.log("sucess")
            } catch (e: any) {
              setMsg(getNetworkErrorMessage())
              console.log(e)
            }
    }

    return (
        <form className="deleteAccountForm" onSubmit={handleDelete}>
            <h1>Delete Account</h1>
            <input className="password" value={password} placeholder="Insert your password" onChange={(e) => setPassword(e.target.value)}/>
            <button className="confirmDeleteAccountBtn" type="submit" disabled={!password}>Confirm Delete</button>
            <p>{msg}</p>
        </form>
    )
}
