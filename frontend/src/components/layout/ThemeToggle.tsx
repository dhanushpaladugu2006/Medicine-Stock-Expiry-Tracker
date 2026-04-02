import { Moon, SunMedium } from 'lucide-react';
import { useThemeStore } from '@/store/theme-store';
import { cn } from '@/lib/utils';

export function ThemeToggle() {
  const { theme, toggleTheme } = useThemeStore();

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className={cn(
        'inline-flex items-center gap-2 rounded-full border border-border/70 px-3 py-2 text-sm font-medium text-ink transition',
        'bg-white/60 backdrop-blur dark:bg-slate-900/60'
      )}
    >
      {theme === 'dark' ? <SunMedium size={16} /> : <Moon size={16} />}
      {theme === 'dark' ? 'Light' : 'Dark'}
    </button>
  );
}
