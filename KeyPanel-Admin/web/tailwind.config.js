export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        navy: '#0C1222',
        smoke: '#1D2438',
        neon: '#21B7FF'
      },
      boxShadow: {
        glow: '0 8px 30px rgba(33,183,255,0.25)'
      }
    }
  },
  plugins: []
};
