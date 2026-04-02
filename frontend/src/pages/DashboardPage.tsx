import { useQuery } from '@tanstack/react-query';
import { BarChart, Bar, CartesianGrid, LineChart, Line, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getDashboardSummary } from '@/api/dashboard';
import { getNotifications } from '@/api/notifications';
import { StatCard } from '@/components/layout/StatCard';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';

export function DashboardPage() {
  const summaryQuery = useQuery({ queryKey: ['dashboard-summary'], queryFn: getDashboardSummary });
  const notificationsQuery = useQuery({ queryKey: ['notifications'], queryFn: getNotifications });

  if (summaryQuery.isLoading) {
    return <Card>Loading dashboard insights...</Card>;
  }

  const summary = summaryQuery.data;

  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Total Medicines" value={summary?.totalMedicines ?? 0} accent="bg-emerald-500" subtitle="All active stock records across your visible branches." />
        <StatCard title="Expiring in 7 Days" value={summary?.expiringIn7Days ?? 0} accent="bg-amber-500" subtitle="Immediate action items for disposal, transfer, or promotion." />
        <StatCard title="Low Stock" value={summary?.lowStockCount ?? 0} accent="bg-orange-500" subtitle="Below reorder level and at risk of missed availability." />
        <StatCard title="Out of Stock" value={summary?.outOfStockCount ?? 0} accent="bg-rose-500" subtitle="Requires urgent replenishment or branch transfer." />
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.2fr_1fr]">
        <Card>
          <div className="mb-5 flex items-center justify-between">
            <div>
              <div className="text-xs uppercase tracking-[0.35em] text-muted">Expiry Hotspots</div>
              <h3 className="mt-2 text-xl font-semibold text-ink">Upcoming expiry windows</h3>
            </div>
            <Badge tone="warn">Scheduler monitored</Badge>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={summary?.expiryTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="label" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#f97316" radius={[10, 10, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card>
          <div className="mb-5">
            <div className="text-xs uppercase tracking-[0.35em] text-muted">Operational Tempo</div>
            <h3 className="mt-2 text-xl font-semibold text-ink">Weekly stock movement</h3>
          </div>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={summary?.stockTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="label" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line dataKey="value" stroke="#0f766e" strokeWidth={3} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <Card>
          <div className="mb-5">
            <div className="text-xs uppercase tracking-[0.35em] text-muted">AI-style Prediction</div>
            <h3 className="mt-2 text-xl font-semibold text-ink">Medicines most likely to expire before sell-through</h3>
          </div>
          <div className="space-y-4">
            {summary?.predictions.map((item) => (
              <div key={item.medicineId} className="rounded-2xl border border-border/70 p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-lg font-semibold text-ink">{item.medicineName}</div>
                    <div className="text-sm text-muted">Batch {item.batchNumber}</div>
                  </div>
                  <Badge tone={item.estimatedDaysToExpiry <= item.estimatedDaysToExhaust ? 'danger' : 'safe'}>
                    {item.estimatedDaysToExpiry} days to expiry
                  </Badge>
                </div>
                <div className="mt-3 text-sm text-muted">Sell-through estimate: {item.estimatedDaysToExhaust === 999 ? 'No recent usage signal' : `${item.estimatedDaysToExhaust} days`}</div>
                <p className="mt-3 text-sm text-ink">{item.recommendation}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <div className="mb-5">
            <div className="text-xs uppercase tracking-[0.35em] text-muted">Alerts Feed</div>
            <h3 className="mt-2 text-xl font-semibold text-ink">Recent notifications</h3>
          </div>
          <div className="space-y-4">
            {notificationsQuery.data?.map((notification) => (
              <div key={notification.id} className="rounded-2xl border border-border/70 p-4">
                <div className="flex items-center justify-between gap-3">
                  <div className="font-medium text-ink">{notification.title}</div>
                  <Badge tone={notification.type === 'LOW_STOCK_ALERT' ? 'warn' : 'danger'}>{notification.type.replaceAll('_', ' ')}</Badge>
                </div>
                <p className="mt-2 text-sm text-muted">{notification.message}</p>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
