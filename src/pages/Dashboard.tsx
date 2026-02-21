import { useEffect } from "react"
import { apiFetch, setAccessToken, clearAccessToken, getAccessToken } from "../api"

import UrlList from "./UrlList";


function Dashboard() {
  return (
    <div>
      <h1>Dashboard</h1>
      <UrlList />
    </div>
  );
}

export default Dashboard;