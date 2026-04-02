import { useQuery, useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { getBranches } from '@/api/branches';
import { register } from '@/api/auth';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useAuthStore } from '@/store/auth-store';

export function RegisterPage() {
  const navigate = useNavigate();
  const setSession = useAuthStore((state) => state.setSession);
  const branchesQuery = useQuery({ queryKey: ['branches'], queryFn: getBranches });

  const mutation = useMutation({
    mutationFn: register,
    onSuccess: (payload) => {
      setSession(payload.accessToken, payload.user);
      toast.success('Account created successfully.');
      navigate('/dashboard');
    },
    onError: () => toast.error('Registration failed. Please verify the form details.')
  });

  return (
    <div className="flex min-h-screen items-center justify-center bg-canvas px-4 py-10">
      <Card className="w-full max-w-3xl p-8 md:p-10">
        <div className="mb-8 flex flex-col gap-2">
          <div className="text-xs uppercase tracking-[0.35em] text-muted">Create Account</div>
          <h1 className="text-3xl font-semibold text-ink">Set up a secure pharmacy workspace</h1>
        </div>

        <form
          className="grid gap-5 md:grid-cols-2"
          onSubmit={(event) => {
            event.preventDefault();
            const formData = new FormData(event.currentTarget);
            mutation.mutate({
              fullName: String(formData.get('fullName')),
              email: String(formData.get('email')),
              password: String(formData.get('password')),
              phone: String(formData.get('phone') || ''),
              role: String(formData.get('role')) as 'ADMIN' | 'PHARMACIST' | 'STAFF',
              branchId: String(formData.get('branchId') || '') || undefined
            });
          }}
        >
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Full name</label>
            <Input name="fullName" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Phone</label>
            <Input name="phone" />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Email</label>
            <Input name="email" type="email" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Password</label>
            <Input name="password" type="password" minLength={8} required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Role</label>
            <select name="role" className="w-full rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60" defaultValue="PHARMACIST">
              <option value="ADMIN">Admin</option>
              <option value="PHARMACIST">Pharmacist</option>
              <option value="STAFF">Staff</option>
            </select>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Branch</label>
            <select name="branchId" className="w-full rounded-xl border border-border bg-white/80 px-4 py-3 text-sm text-ink dark:bg-slate-950/60">
              <option value="">Select branch (optional for admin)</option>
              {branchesQuery.data?.map((branch) => (
                <option key={branch.id} value={branch.id}>{branch.name}</option>
              ))}
            </select>
          </div>
          <div className="md:col-span-2">
            <Button type="submit" className="w-full" disabled={mutation.isPending}>
              {mutation.isPending ? 'Creating account...' : 'Register'}
            </Button>
          </div>
        </form>
        <p className="mt-6 text-sm text-muted">
          Already have an account? <Link className="font-semibold text-accent" to="/login">Go to login</Link>
        </p>
      </Card>
    </div>
  );
}
