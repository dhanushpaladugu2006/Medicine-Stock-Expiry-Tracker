import * as React from 'react';
import { cn } from '@/lib/utils';

export function Card({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('rounded-[28px] border border-border/70 bg-panel/90 p-6 shadow-glow backdrop-blur', className)} {...props} />;
}
