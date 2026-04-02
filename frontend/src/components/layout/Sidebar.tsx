import { Bell, FileText, LayoutDashboard, Package2, Settings2, ShieldCheck } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { cn } from '@/lib/utils';
import { useAuthStore } from '@/store/auth-store';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/medicines', label: 'Medicines', icon: Package2 },
  { to: '/reports', label: 'Reports', icon: FileText },
  { to: '/settings', label: 'Settings', icon: Settings2 }
];

export function Sidebar() {
  const user = useAuthStore((state) => state.user);

  return (
    <aside className="flex w-full flex-col gap-8 rounded-[32px] border border-white/40 bg-[linear-gradient(180deg,rgba(15,118,110,0.92),rgba(15,23,42,0.95))] p-6 text-white shadow-glow lg:w-80">
      <div>
        <div className="inline-flex rounded-full bg-white/15 px-3 py-1 text-xs uppercase tracking-[0.35em] text-white/80">SaaS Ops</div>
        <h1 className="mt-4 text-2xl font-semibold leading-tight">Medicine Stock Expiry Tracker</h1>
        <p className="mt-3 text-sm text-white/75">Branch-aware medicine operations, expiry alerts, and audit-ready reporting.</p>
      </div>

      <div className="rounded-[24px] border border-white/10 bg-white/10 p-4">
        <div className="text-xs uppercase tracking-[0.3em] text-white/60">Signed In</div>
        <div className="mt-3 text-lg font-semibold">{user?.fullName}</div>
        <div className="text-sm text-white/75">{user?.role} {user?.branchName ? `• ${user.branchName}` : ''}</div>
      </div>

      <nav className="space-y-2">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-medium transition',
                isActive ? 'bg-white text-slate-900' : 'text-white/80 hover:bg-white/10 hover:text-white'
              )
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="mt-auto rounded-[24px] border border-white/10 bg-black/15 p-4 text-sm text-white/75">
        <div className="flex items-center gap-2 font-medium text-white">
          <ShieldCheck size={16} /> JWT + RBAC Active
        </div>
        <div className="mt-2 flex items-center gap-2">
          <Bell size={16} /> Expiry notifications and stock alerts are live.
        </div>
      </div>
    </aside>
  );
}
