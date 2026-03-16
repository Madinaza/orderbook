import { Navigate, useLocation } from "react-router-dom";
import { clearToken, isAuthenticated } from "../lib/auth";
import { isTokenExpired } from "../lib/jwt";

export default function ProtectedRoute({ children }) {
  const location = useLocation();

  if (!isAuthenticated() || isTokenExpired()) {
    clearToken();
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}