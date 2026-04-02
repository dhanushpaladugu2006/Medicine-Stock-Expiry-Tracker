import { useEffect, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getNotifications, markNotificationRead } from '@/api/notifications';
import { getProfile, updateProfile } from '@/api/users';
import { ThemeToggle } from '@/components/layout/ThemeToggle';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { useAuthStore } from '@/store/auth-store';

export function SettingsPage() {
  const queryClient = useQueryClient();
  const setSession = useAuthStore((state) => state.setSession);
  const token = useAuthStore((state) => state.token);
  const profileQuery = useQuery({ queryKey: ['profile'], queryFn: getProfile });
  const notificationsQuery = useQuery({ queryKey: ['notifications'], queryFn: getNotifications });
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [emailNotificationsEnabled, setEmailNotificationsEnabled] = useState(true);
  const [smsNotificationsEnabled, setSmsNotificationsEnabled] = useState(false);

  useEffect(() => {
    if (profileQuery.data) {
      setFullName(profileQuery.data.fullName);
      setPhone(profileQuery.data.phone ?? '');
      setEmailNotificationsEnabled(profileQuery.data.emailNotificationsEnabled);
      setSmsNotificationsEnabled(profileQuery.data.smsNotificationsEnabled);
    }
  }, [profileQuery.data]);

  const updateMutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: (profile) => {
      setSession(token!, profile);
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      toast.success('Profile settings saved.');
    },
    onError: () => toast.error('Profile update failed.')
  });

  const readMutation = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['notifications'] })
  });

  return (
    <div className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
      <Card className="space-y-6">
        <div>
          <div className="text-xs uppercase tracking-[0.35em] text-muted">Preferences</div>
          <h2 className="mt-2 text-2xl font-semibold text-ink">Profile and notification settings</h2>
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Full name</label>
          <Input value={fullName} onChange={(event) => setFullName(event.target.value)} />
        </div>
        <div>
          <label className="mb-2 block text-sm font-medium text-ink">Phone</label>
          <Input value={phone} onChange={(event) => setPhone(event.target.value)} />
        </div>
        <label className="flex items-center justify-between rounded-2xl border border-border/70 p-4 text-sm text-ink">
          Email alerts
          <input type="checkbox" checked={emailNotificationsEnabled} onChange={(event) => setEmailNotificationsEnabled(event.target.checked)} />
        </label>
        <label className="flex items-center justify-between rounded-2xl border border-border/70 p-4 text-sm text-ink">
          SMS placeholder alerts
          <input type="checkbox" checked={smsNotificationsEnabled} onChange={(event) => setSmsNotificationsEnabled(event.target.checked)} />
        </label>
        <div className="flex items-center justify-between rounded-2xl border border-border/70 p-4">
          <div>
            <div className="font-medium text-ink">Theme mode</div>
            <div className="text-sm text-muted">Choose the interface mode that works best for your environment.</div>
          </div>
          <ThemeToggle />
        </div>
        <Button
          type="button"
          onClick={() => updateMutation.mutate({ fullName, phone, emailNotificationsEnabled, smsNotificationsEnabled })}
          disabled={updateMutation.isPending}
        >
          {updateMutation.isPending ? 'Saving...' : 'Save settings'}
        </Button>
      </Card>

      <Card className="space-y-5">
        <div>
          <div className="text-xs uppercase tracking-[0.35em] text-muted">Notification History</div>
          <h3 className="mt-2 text-2xl font-semibold text-ink">Recent alert activity</h3>
        </div>
        <div className="space-y-4">
          {notificationsQuery.data?.map((notification) => (
            <div key={notification.id} className="rounded-2xl border border-border/70 p-4">
              <div className="flex items-center justify-between gap-3">
                <div className="font-semibold text-ink">{notification.title}</div>
                <Badge tone={notification.status === 'READ' ? 'safe' : 'warn'}>{notification.status}</Badge>
              </div>
              <p className="mt-2 text-sm text-muted">{notification.message}</p>
              {notification.status !== 'READ' && (
                <button type="button" className="mt-3 text-sm font-semibold text-accent" onClick={() => readMutation.mutate(notification.id)}>
                  Mark as read
                </button>
              )}
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
