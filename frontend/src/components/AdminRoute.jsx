import { Navigate, useLocation } from "react-router-dom";
import { isAuthenticated } from "../lib/auth";
import { ensureValidSession, isAdmin } from "../lib/jwt";

export default function AdminRoute({ children }) {
  const location = useLocation();

  const loggedIn = isAuthenticated() && ensureValidSession();

  if (!loggedIn) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (!isAdmin()) {
    return <Navigate to="/" replace />;
  }

  return children;
}