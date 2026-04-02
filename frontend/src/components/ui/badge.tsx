import { cn } from '@/lib/utils';
import type { ReactNode } from 'react';

const toneMap = {
  safe: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300',
  warn: 'bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-300',
  danger: 'bg-rose-100 text-rose-700 dark:bg-rose-950/50 dark:text-rose-300',
  neutral: 'bg-slate-100 text-slate-700 dark:bg-slate-900 dark:text-slate-300'
};

export function Badge({ children, tone = 'neutral' }: { children: ReactNode; tone?: keyof typeof toneMap }) {
  return <span className={cn('inline-flex rounded-full px-3 py-1 text-xs font-semibold', toneMap[tone])}>{children}</span>;
}
