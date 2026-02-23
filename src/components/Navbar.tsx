import { Link } from "react-router-dom";
import "../css/Navbar.css";
import logo from "../photos/logo.png";
import menu from "../photos/menu.png";



type Props = {
  onToggleSidebar: () => void;
};

function Navbar({ onToggleSidebar }: Props) {
  return (
    <div className="navbar">
      <Link to="/start" className="logo">
        <img src={logo} alt="logo" /> 
      </Link>

      <div className="navRight">
        <Link to="/signin">Signin</Link>
        <Link to="/signup">Signup</Link>
      </div>

      <div className="sideBar" onClick={onToggleSidebar}>
        <img src={menu} alt="menu" /> 
      </div>
    </div>
  );
}

export default Navbar;