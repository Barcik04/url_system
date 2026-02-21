const API_URL = import.meta.env.VITE_API_URL;
import { useEffect, useState } from "react"
import { apiFetch, setAccessToken, clearAccessToken, getAccessToken } from "../api"

function UrlList() {
    const [msg, setMsg] = useState("")

    const page = 0;
    const size = 10;

    async function displayUrls() {
        try {
            const res = await apiFetch(`/api/v1/show-my-links?page=${page}&size=${size}`, {
            method: "POST",
            });


            const data = await res.json();
        } catch {
            setMsg("Error loading your urls")
        }
    }

    useEffect(() => {
        displayUrls();
    }, []);

    return <div>Urls</div>
}

export default UrlList;