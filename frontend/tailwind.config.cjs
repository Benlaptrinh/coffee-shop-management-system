/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: 'var(--bg)',
        surface: 'var(--surface)',
        'surface-muted': 'var(--surface-muted)',
        border: 'var(--border)',
        text: 'var(--text)',
        muted: 'var(--muted)',
        accent: 'var(--accent)',
        'accent-strong': 'var(--accent-strong)',
        'accent-soft': 'var(--accent-soft)',
        'nav-bg': 'var(--nav-bg)',
      },
      boxShadow: {
        soft: 'var(--shadow)',
      },
      fontFamily: {
        sans: ['"Source Sans 3"', '"Segoe UI"', 'sans-serif'],
        serif: ['"Fraunces"', 'serif'],
      },
    },
  },
  plugins: [],
}
