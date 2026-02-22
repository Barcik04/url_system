import "../css/Sidebar.css";

type Props = {
  open: boolean;
  onClose: () => void;
};

function Sidebar({ open, onClose }: Props) {
  return (
    <>
      {/* overlay */}
      {open && <div className="overlay" onClick={onClose} />}

      <div className={`sidebar ${open ? "open" : ""}`}>
        <h3>Menu</h3>
        <ul className="menu">  
          <li>Dashboard</li>
          <li>My links</li>
          <li>Settings</li>
        </ul>
      </div>
    </>
  );
}

export default Sidebar;