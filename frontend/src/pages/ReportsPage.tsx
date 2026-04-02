import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getBranches } from '@/api/branches';
import { downloadReport } from '@/api/reports';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { downloadFile } from '@/lib/utils';
import { useAuthStore } from '@/store/auth-store';

export function ReportsPage() {
  const user = useAuthStore((state) => state.user);
  const [type, setType] = useState('expiry');
  const [format, setFormat] = useState<'csv' | 'pdf'>('csv');
  const [branchId, setBranchId] = useState(user?.branchId ?? '');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const branchesQuery = useQuery({ queryKey: ['branches'], queryFn: getBranches });

  const mutation = useMutation({
    mutationFn: () =>
      downloadReport(type, format, {
        branchId: branchId || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined
      }),
    onSuccess: (blob) => {
      downloadFile(blob, `${type}-report.${format}`);
      toast.success('Report download started.');
    },
    onError: () => toast.error('Unable to export report right now.')
  });

  return (
    <div className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
      <Card className="space-y-5">
        <div>
          <div className="text-xs uppercase tracking-[0.35em] text-muted">Exports</div>
          <h2 className="mt-2 text-2xl font-semibold text-ink">Generate audit-ready pharmacy reports</h2>
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Report type</label>
          <select value={type} onChange={(event) => setType(event.target.value)} className="w-full rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60">
            <option value="expiry">Expiry report</option>
            <option value="stock">Stock report</option>
            <option value="usage">Sales / usage report</option>
          </select>
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Format</label>
          <div className="grid grid-cols-2 gap-3">
            <Button type="button" className={format === 'csv' ? '' : 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'} onClick={() => setFormat('csv')}>CSV</Button>
            <Button type="button" className={format === 'pdf' ? '' : 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'} onClick={() => setFormat('pdf')}>PDF</Button>
          </div>
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Branch</label>
          <select value={branchId} onChange={(event) => setBranchId(event.target.value)} className="w-full rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60" disabled={user?.role !== 'ADMIN'}>
            <option value="">All visible branches</option>
            {branchesQuery.data?.map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}
          </select>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">From date</label>
            <Input type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)} />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">To date</label>
            <Input type="date" value={toDate} onChange={(event) => setToDate(event.target.value)} />
          </div>
        </div>
        <Button type="button" onClick={() => mutation.mutate()} disabled={mutation.isPending}>{mutation.isPending ? 'Preparing export...' : 'Download report'}</Button>
      </Card>

      <Card className="space-y-4">
        <div className="text-xs uppercase tracking-[0.35em] text-muted">Report Catalog</div>
        <div className="rounded-2xl border border-border/70 p-5">
          <h3 className="text-lg font-semibold text-ink">Expiry Report</h3>
          <p className="mt-2 text-sm text-muted">Lists soon-to-expire and expired medicine batches with quantity and branch context.</p>
        </div>
        <div className="rounded-2xl border border-border/70 p-5">
          <h3 className="text-lg font-semibold text-ink">Stock Report</h3>
          <p className="mt-2 text-sm text-muted">Summarizes live inventory, reorder thresholds, and stock status for operational reviews.</p>
        </div>
        <div className="rounded-2xl border border-border/70 p-5">
          <h3 className="text-lg font-semibold text-ink">Usage Report</h3>
          <p className="mt-2 text-sm text-muted">Captures sales and stock deduction activity for compliance and demand planning.</p>
        </div>
      </Card>
    </div>
  );
}
