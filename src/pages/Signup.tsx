import { useState } from "react"
import { useNavigate } from "react-router-dom"
import "../css/Signup.css";

function Signup() {
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [msg, setMsg] = useState("")
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

            const data = await res.json()
            setMsg("account created")
            console.log(data)

        } catch (err) {
            setMsg("Error during register")
            
        }
    }

    return (
        <div className="registerForm">
        <h1>Signup</h1>

        <form onSubmit={handleSubmit}>
            <input placeholder="email" value={username} onChange={(e) => setUsername(e.target.value)}/>
            <br />

            <input placeholder="password" value={password} onChange={(e) => setPassword(e.target.value)}/>
            <br />

            <button type="submit">Create account</button>
            <button type="button" onClick={() => navigate("/signin")}>Go to Sign in</button>
        </form>

        <p>{msg}</p>
        </div>        
    )
}

export default Signup
