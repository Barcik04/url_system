import React from "react";
import { useNavigate } from "react-router-dom";

type Props = {
  children: React.ReactNode;
};

function RequireAuth({ children }: Props) {
  const token = localStorage.getItem("accessToken");
  const navigate = useNavigate();

  if (!token) {
    return (
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          height: "80vh",
          gap: "20px"
        }}
      >
        <div
          style={{
            fontSize: "24px",
            fontWeight: "bold",
            color: "#DC143C"
          }}
        >
          Sign in to have access to dashboard
        </div>

        <button
          style={{
            padding: "5px 10px 5px 10px",
            fontSize: "15px",
            border: "none",
            borderRadius: "3px"
          }}
          onClick={() => navigate("/signin")}
        >
          Sign in
        </button>
      </div>
    );
  }

  return <>{children}</>;
}

export default RequireAuth;