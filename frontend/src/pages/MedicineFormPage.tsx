import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'sonner';
import { getBranches } from '@/api/branches';
import { createMedicine, getMedicine, updateMedicine, uploadMedicineImage } from '@/api/medicines';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useAuthStore } from '@/store/auth-store';
import type { MedicinePayload } from '@/types';

const emptyForm: MedicinePayload = {
  name: '',
  batchNumber: '',
  category: '',
  manufacturer: '',
  quantity: 0,
  reorderLevel: 10,
  price: 0,
  expiryDate: '',
  manufactureDate: '',
  barcode: ''
};

export function MedicineFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const [form, setForm] = useState<MedicinePayload>(emptyForm);
  const [imageFile, setImageFile] = useState<File | null>(null);

  const medicineQuery = useQuery({ queryKey: ['medicine', id], queryFn: () => getMedicine(id!), enabled: Boolean(id) });
  const branchesQuery = useQuery({ queryKey: ['branches'], queryFn: getBranches });

  useEffect(() => {
    if (medicineQuery.data) {
      setForm({
        name: medicineQuery.data.name,
        batchNumber: medicineQuery.data.batchNumber,
        category: medicineQuery.data.category,
        manufacturer: medicineQuery.data.manufacturer,
        quantity: medicineQuery.data.quantity,
        reorderLevel: medicineQuery.data.reorderLevel,
        price: medicineQuery.data.price,
        expiryDate: medicineQuery.data.expiryDate,
        manufactureDate: medicineQuery.data.manufactureDate,
        barcode: medicineQuery.data.barcode,
        branchId: medicineQuery.data.branchId
      });
    }
  }, [medicineQuery.data]);

  const mutation = useMutation({
    mutationFn: async (payload: MedicinePayload) => {
      const saved = id ? await updateMedicine(id, payload) : await createMedicine(payload);
      if (imageFile) {
        await uploadMedicineImage(saved.id, imageFile);
      }
      return saved;
    },
    onSuccess: () => {
      toast.success(`Medicine ${id ? 'updated' : 'created'} successfully.`);
      queryClient.invalidateQueries({ queryKey: ['medicines'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] });
      navigate('/medicines');
    },
    onError: () => toast.error('Unable to save medicine. Please review the fields.')
  });

  return (
    <Card className="space-y-6">
      <div>
        <div className="text-xs uppercase tracking-[0.35em] text-muted">Medicine Editor</div>
        <h2 className="mt-2 text-2xl font-semibold text-ink">{id ? 'Update medicine record' : 'Add a new medicine batch'}</h2>
      </div>

      <form
        className="grid gap-5 md:grid-cols-2"
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate({
            ...form,
            branchId: user?.role === 'ADMIN' ? form.branchId : user?.branchId
          });
        }}
      >
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Medicine name</label>
          <Input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Batch number</label>
          <Input value={form.batchNumber} onChange={(event) => setForm({ ...form, batchNumber: event.target.value })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Category</label>
          <Input value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Manufacturer</label>
          <Input value={form.manufacturer} onChange={(event) => setForm({ ...form, manufacturer: event.target.value })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Quantity</label>
          <Input type="number" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: Number(event.target.value) })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Reorder level</label>
          <Input type="number" value={form.reorderLevel} onChange={(event) => setForm({ ...form, reorderLevel: Number(event.target.value) })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Price</label>
          <Input type="number" step="0.01" value={form.price} onChange={(event) => setForm({ ...form, price: Number(event.target.value) })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Barcode</label>
          <Input value={form.barcode ?? ''} onChange={(event) => setForm({ ...form, barcode: event.target.value })} />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Manufacture date</label>
          <Input type="date" value={form.manufactureDate} onChange={(event) => setForm({ ...form, manufactureDate: event.target.value })} required />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Expiry date</label>
          <Input type="date" value={form.expiryDate} onChange={(event) => setForm({ ...form, expiryDate: event.target.value })} required />
        </div>
        {user?.role === 'ADMIN' && (
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Branch</label>
            <select value={form.branchId ?? ''} onChange={(event) => setForm({ ...form, branchId: event.target.value })} className="w-full rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60" required>
              <option value="">Select branch</option>
              {branchesQuery.data?.map((branch) => <option key={branch.id} value={branch.id}>{branch.name}</option>)}
            </select>
          </div>
        )}
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Medicine image</label>
          <Input type="file" accept="image/*" onChange={(event) => setImageFile(event.target.files?.[0] ?? null)} />
        </div>
        <div className="md:col-span-2 flex gap-3">
          <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Saving...' : 'Save medicine'}</Button>
          <Button type="button" className="bg-slate-900 text-white dark:bg-white dark:text-slate-900" onClick={() => navigate('/medicines')}>Cancel</Button>
        </div>
      </form>
    </Card>
  );
}
