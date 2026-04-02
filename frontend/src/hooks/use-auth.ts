import { useAuthStore } from '@/store/auth-store';

export function useAuth() {
  const { token, user, setSession, clearSession } = useAuthStore();
  return {
    token,
    user,
    isAuthenticated: Boolean(token),
    setSession,
    logout: clearSession
  };
}
