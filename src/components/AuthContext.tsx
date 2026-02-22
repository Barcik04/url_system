// import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

// type AuthContextValue = {
//   accessToken: string | null;
//   setAccessToken: (t: string | null) => void;
//   ready: boolean;
//   refresh: () => Promise<string | null>;
//   signOut: () => void;
// };

// const AuthContext = createContext<AuthContextValue | null>(null);

// export function AuthProvider({ children }: { children: React.ReactNode }) {
//   const [accessToken, setAccessToken] = useState<string | null>(null);
//   const [ready, setReady] = useState(false);

//   const refreshPromiseRef = React.useRef<Promise<string | null> | null>(null);

//   async function refresh(): Promise<string | null> {
//     if (refreshPromiseRef.current) return refreshPromiseRef.current;

//     refreshPromiseRef.current = (async () => {
//       try {
//         const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/refresh`, {
//           method: "POST",
//           credentials: "include", 
//           headers: { "Content-Type": "application/json" },
//         });

//         if (!res.ok) {
//           setAccessToken(null);
//           return null;
//         }

//         const data = await res.json();
//         const token = data.accessToken as string | undefined;

//         if (!token) {
//           setAccessToken(null);
//           return null;
//         }

//         setAccessToken(token);
//         return token;
//       } finally {
//         refreshPromiseRef.current = null;
//       }
//     })();

//     return refreshPromiseRef.current;
//   }

//   function signOut() {
//     setAccessToken(null);
//   }

//   useEffect(() => {
//     refresh().finally(() => setReady(true));
//   }, []);

//   const value = useMemo(
//     () => ({ accessToken, setAccessToken, ready, refresh, signOut }),
//     [accessToken, ready]
//   );

//   return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
// }

// export function useAuth() {
//   const ctx = useContext(AuthContext);
//   if (!ctx) throw new Error("useAuth must be used within AuthProvider");
//   return ctx;
// }