import { useState } from "react"
import { useNavigate } from "react-router-dom"
import "../css/Signup.css";
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling";

function Signup() {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [msg, setMsg] = useState("")
    const [msgType, setMsgType] = useState<"success" | "error" | "">("")
    const navigate = useNavigate()

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        try {
            const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({username, password}),
            })

            if (!res.ok) {
                setMsgType("error")
                setMsg(await getApiErrorMessage(res, "Account creation failed."))
                return
            }

            await res.json().catch(() => null)
            setMsgType("success")
            setMsg("Account created successfully. Please verify it in your email to signin")

        } catch {
            setMsgType("error")
            setMsg(getNetworkErrorMessage())
        }
    }

    return (
        <div className="registerForm">
            <h1>Signup</h1>

            <form onSubmit={handleSubmit}>
                <input placeholder="email" value={username} onChange={(e) => setUsername(e.target.value)}/>

                <input placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)}/>

                <button type="submit">Create account</button>
                <button type="button" onClick={() => navigate("/signin")}>Go to Sign in</button>
            </form>

            <p className={`signup-msg ${msgType}`}>{msg}</p>
        </div>        
    )
}

export default Signup
