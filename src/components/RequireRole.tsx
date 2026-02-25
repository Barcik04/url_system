import React from "react";
import { Navigate } from "react-router-dom";

type Props = {
  children: React.ReactNode;
  role: string;
};

function getRoles(): string[] {
  try {
    const raw = localStorage.getItem("roles");
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.map(String) : [];
  } catch {
    return [];
  }
}

export default function RequireRole({ children, role }: Props) {
  const token = localStorage.getItem("accessToken");
  if (!token) return <h1 style={{color: "red"}}>Cant acess Admin panel</h1>
;

  const roles = getRoles();
  const ok = roles.includes(role) || roles.includes(`ROLE_${role}`);

  if (!ok) return (
    <h1 style={{color: "red"}}>Cant acess Admin panel</h1>
  );

  return <>{children}</>;
}