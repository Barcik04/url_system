import { Link } from "react-router-dom";
import "../css/Navbar.css";

type Props = {
  onToggleSidebar: () => void;
};

function Navbar({ onToggleSidebar }: Props) {
  return (
    <div className="navbar">
      <Link to="/dashboard" className="logo">
        URL Shortener
      </Link>

      <div className="navRight">
        <Link to="/signin">Sign in</Link>
        <Link to="/signup">Signup</Link>
      </div>

      <div className="sideBar" onClick={onToggleSidebar}>
        Sidebar show
      </div>
    </div>
  );
}

export default Navbar;