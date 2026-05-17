/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'],
        mono: ['JetBrains Mono', 'ui-monospace', 'monospace'],
      },
      colors: {
        brand: {
          50: '#eef9ff',
          100: '#d9f1ff',
          200: '#bbe6ff',
          300: '#8ad6ff',
          400: '#51bdff',
          500: '#289dff',
          600: '#127df5',
          700: '#0c64d8',
          800: '#1051af',
          900: '#13468a',
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'hero-glow':
          'radial-gradient(ellipse at top, rgba(18, 125, 245, 0.25) 0%, rgba(122, 79, 255, 0.10) 35%, transparent 70%)',
        'auth-aurora':
          'radial-gradient(at 20% 20%, rgba(18, 125, 245, 0.35), transparent 50%), radial-gradient(at 80% 30%, rgba(168, 85, 247, 0.30), transparent 55%), radial-gradient(at 50% 90%, rgba(34, 211, 238, 0.20), transparent 60%)',
      },
      boxShadow: {
        glow: '0 0 40px -10px rgba(40, 157, 255, 0.55)',
        'glow-emerald': '0 0 40px -10px rgba(16, 185, 129, 0.55)',
        'glow-rose': '0 0 40px -10px rgba(244, 63, 94, 0.55)',
        'glow-amber': '0 0 40px -10px rgba(245, 158, 11, 0.55)',
      },
      animation: {
        'fade-in': 'fade-in 0.4s ease-out both',
        'slide-up': 'slide-up 0.5s cubic-bezier(0.22, 1, 0.36, 1) both',
        'slide-down': 'slide-down 0.5s cubic-bezier(0.22, 1, 0.36, 1) both',
        'scale-in': 'scale-in 0.4s cubic-bezier(0.22, 1, 0.36, 1) both',
        'pulse-slow': 'pulse 3s ease-in-out infinite',
        'aurora': 'aurora 18s ease infinite',
        'shimmer': 'shimmer 2.5s linear infinite',
        'bg-pan': 'bg-pan 20s ease infinite',
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'slide-up': {
          '0%': { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'slide-down': {
          '0%': { opacity: '0', transform: 'translateY(-16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'scale-in': {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        aurora: {
          '0%, 100%': { transform: 'translate(0, 0) scale(1)' },
          '50%': { transform: 'translate(2%, -2%) scale(1.05)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
        'bg-pan': {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
      },
    },
  },
  plugins: [],
};
