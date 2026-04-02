import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { toast } from 'sonner';
import { bulkUpload, deleteMedicine, getMedicines } from '@/api/medicines';
import { getBranches } from '@/api/branches';
import { adjustStock, getStockHistory } from '@/api/stocks';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { formatDate } from '@/lib/utils';
import { useAuthStore } from '@/store/auth-store';
import type { Medicine } from '@/types';

function statusTone(status: string) {
  if (status === 'SAFE') return 'safe';
  if (status === 'OUT_OF_STOCK' || status === 'EXPIRED') return 'danger';
  return 'warn';
}

export function MedicinesPage() {
  const user = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [branchId, setBranchId] = useState<string | undefined>(user?.branchId);
  const [selectedMedicine, setSelectedMedicine] = useState<Medicine | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const medicinesQuery = useQuery({
    queryKey: ['medicines', search, category, branchId],
    queryFn: () => getMedicines({ search, category, branchId, size: 50 })
  });
  const branchesQuery = useQuery({ queryKey: ['branches'], queryFn: getBranches });
  const historyQuery = useQuery({
    queryKey: ['stock-history', selectedMedicine?.id],
    queryFn: () => getStockHistory(selectedMedicine!.id),
    enabled: Boolean(selectedMedicine)
  });

  const deleteMutation = useMutation({
    mutationFn: deleteMedicine,
    onSuccess: () => {
      toast.success('Medicine archived successfully.');
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
    },
    onError: () => toast.error('Could not archive medicine.')
  });

  const stockMutation = useMutation({
    mutationFn: adjustStock,
    onSuccess: () => {
      toast.success('Stock updated in real time.');
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
      queryClient.invalidateQueries({ queryKey: ['stock-history', selectedMedicine?.id] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] });
    },
    onError: () => toast.error('Stock update failed.')
  });

  const bulkMutation = useMutation({
    mutationFn: ({ file, targetBranchId }: { file: File; targetBranchId?: string }) => bulkUpload(file, targetBranchId),
    onSuccess: (result) => {
      toast.success(`Bulk upload finished: ${result.created} created, ${result.updated} updated.`);
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
    },
    onError: () => toast.error('Bulk upload failed.')
  });

  const categories = useMemo(() => {
    const data = medicinesQuery.data?.content ?? [];
    return Array.from(new Set(data.map((item) => item.category))).sort();
  }, [medicinesQuery.data]);

  const handleFile = (file?: File) => {
    if (!file) return;
    bulkMutation.mutate({ file, targetBranchId: branchId });
  };

  return (
    <div className="space-y-6">
      <Card className="space-y-5">
        <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <div className="text-xs uppercase tracking-[0.35em] text-muted">Inventory Command</div>
            <h2 className="mt-2 text-2xl font-semibold text-ink">Medicines and branch stock</h2>
          </div>
          <Link to="/medicines/new">
            <Button>Add medicine</Button>
          </Link>
        </div>
        <div className="grid gap-4 md:grid-cols-4">
          <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by name, batch, category" />
          <select value={category} onChange={(event) => setCategory(event.target.value)} className="rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60">
            <option value="">All categories</option>
            {categories.map((item) => <option key={item} value={item}>{item}</option>)}
          </select>
          <select value={branchId ?? ''} onChange={(event) => setBranchId(event.target.value || undefined)} className="rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60" disabled={user?.role !== 'ADMIN'}>
            <option value="">All branches</option>
            {branchesQuery.data?.map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}
          </select>
          <label
            className={`flex cursor-pointer items-center justify-center rounded-xl border border-dashed px-4 py-3 text-sm font-medium transition ${isDragging ? 'border-accent bg-accent/10 text-accent' : 'border-border bg-white/60 text-muted hover:border-accent hover:text-accent dark:bg-slate-950/40'}`}
            onDragOver={(event) => {
              event.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={(event) => {
              event.preventDefault();
              setIsDragging(false);
              handleFile(event.dataTransfer.files?.[0]);
            }}
          >
            {bulkMutation.isPending ? 'Uploading CSV...' : 'Drag CSV here or click'}
            <input
              type="file"
              accept=".csv"
              className="hidden"
              onChange={(event) => handleFile(event.target.files?.[0])}
            />
          </label>
        </div>
      </Card>

      <div className="grid gap-6 xl:grid-cols-[1.3fr_0.9fr]">
        <Card className="overflow-hidden p-0">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-100/80 text-muted dark:bg-slate-900/80">
                <tr>
                  <th className="px-5 py-4">Medicine</th>
                  <th className="px-5 py-4">Branch</th>
                  <th className="px-5 py-4">Expiry</th>
                  <th className="px-5 py-4">Stock</th>
                  <th className="px-5 py-4">Status</th>
                  <th className="px-5 py-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {medicinesQuery.data?.content.map((medicine) => (
                  <tr key={medicine.id} className="border-t border-border/60">
                    <td className="px-5 py-4">
                      <button type="button" onClick={() => setSelectedMedicine(medicine)} className="text-left">
                        <div className="font-semibold text-ink">{medicine.name}</div>
                        <div className="text-xs text-muted">Batch {medicine.batchNumber} • Risk {medicine.predictedExpiryRiskScore}%</div>
                      </button>
                    </td>
                    <td className="px-5 py-4 text-muted">{medicine.branchName}</td>
                    <td className="px-5 py-4 text-muted">{formatDate(medicine.expiryDate)}</td>
                    <td className="px-5 py-4 text-muted">{medicine.quantity}</td>
                    <td className="px-5 py-4"><Badge tone={statusTone(medicine.status) as 'safe' | 'warn' | 'danger'}>{medicine.status.replaceAll('_', ' ')}</Badge></td>
                    <td className="px-5 py-4">
                      <div className="flex flex-wrap gap-2">
                        <Link to={`/medicines/${medicine.id}/edit`} className="rounded-full border border-border px-3 py-1 text-xs font-semibold text-ink">Edit</Link>
                        <button type="button" className="rounded-full border border-border px-3 py-1 text-xs font-semibold text-ink" onClick={() => stockMutation.mutate({ medicineId: medicine.id, quantityChange: -1, type: 'SALE', referenceNote: 'Sale/usage deduction' })}>Sell -1</button>
                        <button type="button" className="rounded-full border border-border px-3 py-1 text-xs font-semibold text-ink" onClick={() => stockMutation.mutate({ medicineId: medicine.id, quantityChange: 10, type: 'PURCHASE', referenceNote: 'Restock quick action', unitPrice: medicine.price })}>Restock +10</button>
                        <button type="button" className="rounded-full border border-rose-200 px-3 py-1 text-xs font-semibold text-rose-600" onClick={() => deleteMutation.mutate(medicine.id)}>Archive</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <Card>
          {selectedMedicine ? (
            <div className="space-y-5">
              <div>
                <div className="text-xs uppercase tracking-[0.35em] text-muted">Stock History</div>
                <h3 className="mt-2 text-xl font-semibold text-ink">{selectedMedicine.name}</h3>
                <p className="text-sm text-muted">{selectedMedicine.branchName} • Batch {selectedMedicine.batchNumber}</p>
              </div>
              <div className="space-y-3">
                {historyQuery.data?.map((entry) => (
                  <div key={entry.id} className="rounded-2xl border border-border/70 p-4">
                    <div className="flex items-center justify-between gap-3">
                      <Badge tone={entry.quantityChange < 0 ? 'warn' : 'safe'}>{entry.type}</Badge>
                      <div className="text-sm font-semibold text-ink">{entry.quantityChange > 0 ? '+' : ''}{entry.quantityChange}</div>
                    </div>
                    <div className="mt-2 text-sm text-muted">{entry.referenceNote || 'Manual stock activity'} • {formatDate(entry.transactionDate)}</div>
                    <div className="mt-1 text-xs text-muted">After transaction: {entry.quantityAfter}</div>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-sm text-muted">Select a medicine row to inspect recent stock history and movement logs.</div>
          )}
        </Card>
      </div>
    </div>
  );
}
