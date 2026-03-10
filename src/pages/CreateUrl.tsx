import { useState } from "react"
import { apiFetch } from "../api"
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling"
import "../css/CreateUrl.css";

function newIdempotencyKey() {
  return (crypto as any).randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
}

type Props = {
  onCreated?: (shortUrl: string) => void;
};

function CreateUrl({ onCreated }: Props) {
    const [longUrl, setLongUrl] = useState("")
    const [expiredAt, setExpiredAt] = useState("")
    const [msg, setMsg] = useState("")
    const [msgType, setMsgType] = useState<"success" | "error" | "">("")
    const [idempotencyKey, setIdempotencyKey] = useState(() => newIdempotencyKey())

    async function handleUrlCreate(e: React.FormEvent) {
        e.preventDefault()

        try {
            const res = await apiFetch("/api/v1/urls", {
                method: "POST",
                headers: { "Idempotency-Key": idempotencyKey },
                body: JSON.stringify({ longUrl, expiredAt }),
            })

            if (!res.ok) {
                setMsgType("error")
                setMsg(await getApiErrorMessage(res, "Failed to create short URL."))
                return
            }

            const data = await res.json()
            setMsgType("success")
            setMsg("Short URL created successfully.")
            setLongUrl("")
            setExpiredAt("")
            setIdempotencyKey(newIdempotencyKey())

            const short = `${import.meta.env.VITE_API_URL}/${data.shortUrl}`;
            onCreated?.(short);
        } catch (e) {
            setMsgType("error")
            setMsg(getNetworkErrorMessage())
            console.log(e)
        }
    }

    return (
        <form className="createUrlForm" onSubmit={handleUrlCreate}>
            <h1>Create Url</h1>
            <input
                className="longUrl"
                value={longUrl}
                placeholder="Insert your url to convert"
                onChange={(e) => setLongUrl(e.target.value)}
            />
            <h2>Expiry date (Optional)</h2>
            <input
                className="expiryDate"
                value={expiredAt}
                placeholder="date of expiry"
                onChange={(e) => setExpiredAt(e.target.value)}
                type="date"
            />
            <button className="createUrlBtn" type="submit" disabled={!longUrl}>
                Create
            </button>
            <p className={`create-url-msg ${msgType}`}>{msg}</p>
        </form>
    )
}

export default CreateUrl