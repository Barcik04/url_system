import { useState } from "react";
import { apiFetch } from "../api";

import "../css/AdminPanel.css"


function toIsoOrUndefined(dateOnly: string) {
  if (!dateOnly) return undefined;
  return new Date(`${dateOnly}T23:59:59`).toISOString();
}



type Props = {
  code: string;
  onClose: () => void;
  onSuccess: () => void;
};


export default function PatchUrlBtn({ code }: Props) {
    const [longUrl, setLongUrl] = useState("")
    const [expiredAt, setExpiredAt] = useState("")
    const [msg, setMsg] = useState("")


    async function handlePatch(e: React.FormEvent) {
        e.preventDefault()

        try {
            setMsg("");

            const payload: any = {};

            if (longUrl.trim() !== "") payload.longUrl = longUrl.trim();

            const iso = toIsoOrUndefined(expiredAt);
            if (iso) payload.expiredAt = iso;


            const res = await apiFetch(`/api/v1/patch-url/${code}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                const txt = await res.text().catch(() => "");
                throw new Error(txt || `HTTP ${res.status}`);
            }
            
            setMsg("Patch Success")
            } catch (e: any) {
            setMsg(e?.message ?? "Patch failed");
            }
    }

    return (
        <form className="createUrlForm" onSubmit={handlePatch}>
            <h1>Patch Url</h1>
            <input className="longUrl" value={longUrl} placeholder="Insert your url to convert" onChange={(e) => setLongUrl(e.target.value)} />
            <h2>Expiry date (Optional)</h2>
            <input className="expiryDate" value={expiredAt} placeholder="date of expiry" onChange={(e) => setExpiredAt(e.target.value)} type="date" />
            <button className="createUrlBtn" type="submit">Create</button>
            <p>{msg}</p>
        </form>
    )
}