import "../css/Sidebar.css";
import { Link, useNavigate } from "react-router-dom";
import { clearAccessToken } from "../api";


type Props = {
  open: boolean;
  onClose: () => void;
};

function Sidebar({ open, onClose }: Props) {
  const navigate = useNavigate();


  async function handleLogout() {
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/auth/logout`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          ...(localStorage.getItem("accessToken")
            ? { Authorization: `Bearer ${localStorage.getItem("accessToken")}` }
            : {}),
        },
      });
    } catch (e) {
      console.log("logout request failed (ok)");
    }

    clearAccessToken();
    localStorage.removeItem("roles");

    navigate("/signin");
    onClose();
  }



  return (
    <>
      {/* overlay */}
      {open && <div className="overlay" onClick={onClose} />}

      <div className={`sidebar ${open ? "open" : ""}`}>
        <div className="menuWrapper">
          <h3>Menu</h3>
          <ul className="menu">  
            <li>
              <Link to="/dashboard" onClick={onClose}>Dashboard</Link>
            </li>

            <li>
              <Link to="/admin" onClick={onClose}>Admin</Link>
            </li>

            <li>
              <Link to="/settings" onClick={onClose}>Settings</Link>
            </li>
          </ul> 
        </div>
          <button className="logoutBtn" onClick={handleLogout}>
              Logout
          </button>
      </div>
    </>
  );
}

export default Sidebar;