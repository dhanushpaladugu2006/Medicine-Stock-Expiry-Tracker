import { LogOut, Wifi, WifiOff } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useOnlineStatus } from '@/hooks/use-online-status';
import { useAuthStore } from '@/store/auth-store';
import { ThemeToggle } from '@/components/layout/ThemeToggle';
import { Button } from '@/components/ui/button';

export function Header() {
  const online = useOnlineStatus();
  const navigate = useNavigate();
  const clearSession = useAuthStore((state) => state.clearSession);

  return (
    <header className="flex flex-col gap-4 rounded-[28px] border border-border/60 bg-panel/80 p-5 backdrop-blur md:flex-row md:items-center md:justify-between">
      <div>
        <div className="text-xs uppercase tracking-[0.35em] text-muted">Operations Hub</div>
        <h2 className="mt-2 text-2xl font-semibold text-ink">Stay ahead of low stock and expiring batches</h2>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        <div className={`inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium ${online ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-300' : 'bg-amber-100 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300'}`}>
          {online ? <Wifi size={16} /> : <WifiOff size={16} />}
          {online ? 'Online sync active' : 'Offline mode active'}
        </div>
        <ThemeToggle />
        <Button
          type="button"
          className="bg-slate-900 text-white dark:bg-white dark:text-slate-900"
          onClick={() => {
            clearSession();
            navigate('/login');
          }}
        >
          <LogOut size={16} className="mr-2" />
          Logout
        </Button>
      </div>
    </header>
  );
}
