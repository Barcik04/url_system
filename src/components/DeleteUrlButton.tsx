import { useState } from "react";
import { apiFetch } from "../api";
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling";

import "../css/AdminPanel.css"

import bin from "../photos/bin.png"

type Props = {
  code: string; 
  onDeleted?: () => void;       
};


export default function DeleteUrlButton({ code, onDeleted }: Props) {
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    async function handleDelete() {
        const ok = window.confirm(`Delete URL: ${code}?`);
        if (!ok) return;

        try {
            setLoading(true);
            setErr(null);

            const res = await apiFetch(`/api/v1/delete-url/${code}`, {
                method: "DELETE",
            });

            if (!res.ok) {
                setErr(await getApiErrorMessage(res, "Failed to delete URL."));
                return;
            }

            onDeleted?.();
            } catch {
            setErr(getNetworkErrorMessage());
            } finally {
            setLoading(false);
            }
    }

    return (
        <div>
            <button className="deleteBtn" onClick={handleDelete} disabled={loading}>
                {loading ? "Deleting..." : <img src={bin} alt="bin" />}
            </button>
            {err && <p>{err}</p>}
        </div>
    );
}