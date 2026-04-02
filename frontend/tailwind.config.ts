import type { Config } from 'tailwindcss';

export default {
  darkMode: ['class'],
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        canvas: 'var(--color-canvas)',
        panel: 'var(--color-panel)',
        accent: 'var(--color-accent)',
        ink: 'var(--color-ink)',
        muted: 'var(--color-muted)',
        border: 'var(--color-border)',
        warning: 'var(--color-warning)',
        danger: 'var(--color-danger)'
      },
      boxShadow: {
        glow: '0 20px 80px rgba(15, 118, 110, 0.18)'
      },
      borderRadius: {
        xl: '1.25rem',
        '2xl': '1.75rem'
      }
    }
  },
  plugins: []
} satisfies Config;
