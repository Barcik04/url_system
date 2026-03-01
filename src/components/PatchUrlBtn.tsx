import { useState } from "react";
import { apiFetch } from "../api";

import "../css/AdminPanel.css"

import bin from "../photos/bin.png"

type Props = {
  code: string; 
  onDeleted?: () => void;       
};


export default function PatchUrlBtn({ code, onDeleted }: Props) {
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    async function handlePatch() {
        const ok = window.confirm(`Delete URL: ${code}?`);
        if (!ok) return;

        try {
            setLoading(true);
            setErr(null);

            const res = await apiFetch(`/api/v1/patch-url/${code}`, {
                method: "PATCH",
            });

            if (!res.ok) {
                const txt = await res.text().catch(() => "");
                throw new Error(txt || `HTTP ${res.status}`);
            }

            onDeleted?.();
            } catch (e: any) {
            setErr(e?.message ?? "Delete failed");
            } finally {
            setLoading(false);
            }
    }

    return (
        <div>
            <button className="patchBtn" onClick={handlePatch} disabled={loading}>
                {loading ? "Deleting..." : <img src={bin} alt="bin" />}PPPPP
            </button>
        </div>
    );
}