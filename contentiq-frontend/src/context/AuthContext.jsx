import { createContext, useContext, useEffect, useState } from 'react';
import { auth as authApi } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('contentiq_token');
    const storedUser = localStorage.getItem('contentiq_user');
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('contentiq_user');
      }
    }
    setReady(true);
  }, []);

  const persist = (resp) => {
    localStorage.setItem('contentiq_token', resp.token);
    const u = { username: resp.username, userId: resp.userId };
    localStorage.setItem('contentiq_user', JSON.stringify(u));
    setUser(u);
  };

  const login = async (username, password) => {
    const resp = await authApi.login(username, password);
    persist(resp);
    return resp;
  };

  const register = async (username, email, password) => {
    const resp = await authApi.register(username, email, password);
    persist(resp);
    return resp;
  };

  const logout = () => {
    localStorage.removeItem('contentiq_token');
    localStorage.removeItem('contentiq_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, ready, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
