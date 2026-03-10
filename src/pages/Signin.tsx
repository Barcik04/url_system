import { useState } from "react"
import { apiFetch, setAccessToken } from "../api"
import { getApiErrorMessage, getNetworkErrorMessage } from "../errorHandling"
import { useNavigate } from "react-router-dom"
import "../css/Signin.css";


function parseJwt(token: string): any | null {
  try {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}



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

      if (!res.ok) {
        setMsg(await getApiErrorMessage(res, "Sign in failed."))
        return
      }

      const data = await res.json()
      setAccessToken(data.accessToken);

      localStorage.setItem("roles", JSON.stringify(data.roles ?? []));
      localStorage.setItem("username", data.username ?? "");

      navigate("/dashboard");
      setMsg("signin success")
    } catch {
      setMsg(getNetworkErrorMessage())
    }
  }

  return (
    <form className="loginForm" onSubmit={handleSubmit}>
      <h1>Signin</h1>
      <input value={username} placeholder="email" onChange={(e) => setUsername(e.target.value)} />
      <input value={password} placeholder="password" onChange={(e) => setPassword(e.target.value)} type="password" />
      <button type="submit">Sign in</button>
      <button type="button" onClick={() => navigate("/signup")}>I Dont have an account yet</button>
      <p>{msg}</p>
    </form>
  )
}

export default Signin