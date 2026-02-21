import { useState } from "react"
import { apiFetch, setAccessToken } from "../api"
import { useNavigate } from "react-router-dom"
import "../css/Signin.css";


function Signin() {
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [msg, setMsg] = useState("")
  const navigate = useNavigate()


  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()

    try {
      const res = await apiFetch("/auth/signin", {
        method: "POST",
        body: JSON.stringify({ username, password }),
      })

      const data = await res.json().catch(() => null)

      if (!res.ok) {
        setMsg(data?.message ?? `Signin failed (${res.status})`)
        return
      }

      setAccessToken(data.accessToken)
      
      navigate("/dashboard")
      setMsg("signin success")
    } catch {
      setMsg("Network error")
    }
  }

  return (
    <form className="loginForm" onSubmit={handleSubmit}>
      <h1>Signin</h1>
      <input value={username} placeholder="email" onChange={(e) => setUsername(e.target.value)} />
      <input value={password} placeholder="password" onChange={(e) => setPassword(e.target.value)} type="password" />
      <button type="submit">Sign in</button>
      <p>{msg}</p>
    </form>
  )
}

export default Signin