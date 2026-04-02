import { QueryClientProvider } from '@tanstack/react-query';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { Toaster } from 'sonner';
import { registerSW } from 'virtual:pwa-register';
import App from '@/App';
import { queryClient } from '@/lib/query-client';
import { useThemeStore } from '@/store/theme-store';
import '@/index.css';

const storedTheme = useThemeStore.getState().theme;
document.documentElement.classList.toggle('dark', storedTheme === 'dark');
registerSW({ immediate: true });

createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={queryClient}>
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <App />
      <Toaster richColors position="top-right" />
    </BrowserRouter>
  </QueryClientProvider>
);
