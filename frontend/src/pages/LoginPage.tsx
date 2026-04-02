import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'sonner';
import { login } from '@/api/auth';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useAuthStore } from '@/store/auth-store';

export function LoginPage() {
  const navigate = useNavigate();
  const setSession = useAuthStore((state) => state.setSession);

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (payload) => {
      setSession(payload.accessToken, payload.user);
      toast.success('Welcome back to the operations hub.');
      navigate('/dashboard');
    },
    onError: () => toast.error('Unable to sign in. Check your credentials and try again.')
  });

  return (
    <div className="flex min-h-screen items-center justify-center bg-canvas px-4 py-10">
      <Card className="grid max-w-5xl gap-8 overflow-hidden p-0 md:grid-cols-[1.2fr_1fr]">
        <div className="bg-[linear-gradient(140deg,rgba(15,118,110,0.95),rgba(249,115,22,0.82))] p-8 text-white md:p-12">
          <div className="text-xs uppercase tracking-[0.4em] text-white/70">Medicine SaaS</div>
          <h1 className="mt-6 text-4xl font-semibold leading-tight">Protect inventory margins before medicine batches expire.</h1>
          <p className="mt-6 max-w-md text-white/80">Track stock in real time, notify branches early, and keep every edit traceable through audit logs and report exports.</p>
        </div>

        <form
          className="space-y-5 p-8 md:p-10"
          onSubmit={(event) => {
            event.preventDefault();
            const formData = new FormData(event.currentTarget);
            mutation.mutate({
              email: String(formData.get('email')),
              password: String(formData.get('password'))
            });
          }}
        >
          <div>
            <div className="text-xs uppercase tracking-[0.35em] text-muted">Sign In</div>
            <h2 className="mt-3 text-3xl font-semibold text-ink">Welcome back</h2>
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Email</label>
            <Input name="email" type="email" placeholder="admin@pharmacy.com" required />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-ink">Password</label>
            <Input name="password" type="password" placeholder="Enter your password" required />
          </div>
          <Button type="submit" className="w-full" disabled={mutation.isPending}>
            {mutation.isPending ? 'Signing in...' : 'Login'}
          </Button>
          <p className="text-sm text-muted">
            Need a new workspace account? <Link className="font-semibold text-accent" to="/register">Register here</Link>
          </p>
        </form>
      </Card>
    </div>
  );
}
