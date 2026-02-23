import { useEffect, useState } from "react"

import CreateUrl from "./CreateUrl"
import UrlList from "./UrlList";
import "../css/UrlList.css"
import "../css/Dashboard.css"



function Dashboard() {
  const [open, setOpen] = useState(false);

  return (
    <div className="dashboard">
      <div className="dashboardInner">
        <div className="dashboardHeader">
          <h1 className="h1Dashboard">Dashboard</h1>

          <div className="dashboardButtons">
            <button className="createBtn" onClick={() => setOpen(true)}>
              Create Url
            </button>
        
          </div>
          
        </div>

        <UrlList />
      </div>

      {open && (
        <div className="overlay" onClick={() => setOpen(false)}>
          <div onClick={(e) => e.stopPropagation()}>
            <CreateUrl />
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;