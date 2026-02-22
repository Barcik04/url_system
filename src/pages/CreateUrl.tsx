import { useState } from "react"
import { apiFetch } from "../api"
import "../css/CreateUrl.css";

function newIdempotencyKey() {
  return (crypto as any).randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
}



function CreateUrl() {
    const [longUrl, setLongUrl] = useState("")
    const [expiredAt, setExpiredAt] = useState("")
    const [msg, setMsg] = useState("")
    const [idempotencyKey, setIdempotencyKey] = useState(() => newIdempotencyKey())


    async function handleUrlCreate(e: React.FormEvent) {
        e.preventDefault()

        try {
            const res = await apiFetch("/api/v1/urls", {
                method: "POST",
                headers: {"Idempotency-Key": idempotencyKey,},
                body: JSON.stringify({ longUrl, expiredAt }),
            })


            const data = await res.json().catch(() => null)

            if (!res.ok) {
                setMsg(data?.message ?? `Creation Failed (${res.status})`)
                return
            }

            setMsg("Creation Success")
            setIdempotencyKey(newIdempotencyKey())
        } catch(e) {
            setMsg("Network error")
            console.log(e)
        }
    }

    return (
        <form className="createUrlForm" onSubmit={handleUrlCreate}>
            <h1>Create Url</h1>
            <input value={longUrl} placeholder="url" onChange={(e) => setLongUrl(e.target.value)} />
            <input value={expiredAt} placeholder="date of expiry" onChange={(e) => setExpiredAt(e.target.value)} type="date" />
            <button type="submit" disabled={!longUrl}>Create</button>
            <p>{msg}</p>
        </form>
    )
}

export default CreateUrl