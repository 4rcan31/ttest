import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import { readSession, writeSession } from '../api/client.js';
import { authApi } from '../api/services.js';

const AuthContext = createContext(null);

function toSession(authResponse) {
  return {
    token: authResponse.accessToken,
    expiresAt: Date.now() + authResponse.expiresIn * 1000,
    user: authResponse.user,
  };
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => readSession());
  const navigate = useNavigate();

  const applySession = useCallback((next) => {
    writeSession(next);
    setSession(next);
  }, []);

  const logout = useCallback(() => applySession(null), [applySession]);

  // Token inválido o expirado detectado por el cliente HTTP.
  useEffect(() => {
    const onUnauthorized = () => {
      applySession(null);
      navigate('/login?expirada=1', { replace: true });
    };
    window.addEventListener('sportshop:unauthorized', onUnauthorized);
    return () => window.removeEventListener('sportshop:unauthorized', onUnauthorized);
  }, [applySession, navigate]);

  // Cierre de sesión automático al vencer el token.
  useEffect(() => {
    if (!session) return undefined;
    const timeout = setTimeout(logout, Math.max(session.expiresAt - Date.now(), 0));
    return () => clearTimeout(timeout);
  }, [session, logout]);

  const login = useCallback(async (email, password) => {
    const response = await authApi.login(email, password);
    applySession(toSession(response));
    return response.user;
  }, [applySession]);

  const register = useCallback(async (data) => {
    const response = await authApi.register(data);
    applySession(toSession(response));
    return response.user;
  }, [applySession]);

  const updateUser = useCallback((user) => {
    setSession((current) => {
      if (!current) return current;
      const next = { ...current, user };
      writeSession(next);
      return next;
    });
  }, []);

  const value = useMemo(() => ({
    user: session?.user ?? null,
    isAuthenticated: Boolean(session),
    isAdmin: session?.user?.role === 'ADMIN',
    login,
    register,
    logout,
    updateUser,
  }), [session, login, register, logout, updateUser]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return context;
}
