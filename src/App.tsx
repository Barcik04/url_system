import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";

import Signup from "./pages/Signup";
import Signin from "./pages/Signin";
import Dashboard from "./pages/Dashboard";
import AdminPanel from "./pages/AdminPanel";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";
import Start from "./pages/Start";
import RequireAuth from "./components/RequireAuth";
import RequireRole from "./components/RequireRole";
import Settings from "./pages/Settings";
import Subscriptions from "./pages/Subscriptions";
import ChatWidget from "./components/ChatWidget"; // <- dodaj to

function PageTransition({ children }: { children: React.ReactNode }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -8 }}
      transition={{ duration: 0.19, ease: "easeOut" }}
      style={{ width: "100%" }}
    >
      {children}
    </motion.div>
  );
}

function AnimatedRoutes() {
  const location = useLocation();

  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<Navigate to="/signup" replace />} />

        <Route
          path="/signup"
          element={
            <PageTransition>
              <Signup />
            </PageTransition>
          }
        />

        <Route
          path="/signin"
          element={
            <PageTransition>
              <Signin />
            </PageTransition>
          }
        />

        <Route
          path="/dashboard"
          element={
            <PageTransition>
              <RequireAuth>
                <Dashboard />
              </RequireAuth>
            </PageTransition>
          }
        />

        <Route
          path="/start"
          element={
            <PageTransition>
              <Start />
            </PageTransition>
          }
        />

        <Route
          path="/admin"
          element={
            <PageTransition>
              <RequireRole role="ADMIN">
                <AdminPanel />
              </RequireRole>
            </PageTransition>
          }
        />

        <Route
          path="/settings"
          element={
            <PageTransition>
              <RequireAuth>
                <Settings />
              </RequireAuth>
            </PageTransition>
          }
        />

        <Route
          path="/subscriptions"
          element={
            <PageTransition>
              <RequireAuth>
                <Subscriptions />
              </RequireAuth>
            </PageTransition>
          }
        />
      </Routes>
    </AnimatePresence>
  );
}

function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <BrowserRouter>
      <Navbar onToggleSidebar={() => setSidebarOpen(v => !v)} />
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <AnimatedRoutes />

      <ChatWidget /> {/* <- tutaj */}
    </BrowserRouter>
  );
}

export default App;