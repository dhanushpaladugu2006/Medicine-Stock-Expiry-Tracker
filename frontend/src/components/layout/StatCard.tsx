import type { ReactNode } from 'react';
import { Card } from '@/components/ui/card';

export function StatCard({ title, value, accent, subtitle }: { title: string; value: ReactNode; accent: string; subtitle: string }) {
  return (
    <Card className="relative overflow-hidden">
      <div className={`absolute inset-x-0 top-0 h-1 ${accent}`} />
      <div className="text-sm font-medium text-muted">{title}</div>
      <div className="mt-4 text-4xl font-semibold text-ink">{value}</div>
      <p className="mt-3 text-sm text-muted">{subtitle}</p>
    </Card>
  );
}
