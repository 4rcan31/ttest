import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';

/** Restringe rutas a usuarios autenticados (y opcionalmente a un rol). */
export default function ProtectedRoute({ role }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  if (role && user.role !== role) {
    return <Navigate to="/" replace />;
  }
  return <Outlet />;
}
