import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      // Proxy API calls starting with /auth to the backend to avoid CORS in dev
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // if backend is not on root, you can rewrite here; we keep path as-is
        // rewrite: (path) => path.replace(/^\/auth/, '/auth'),
        secure: false,
      },
      // Also proxy general API routes if your backend exposes them under /api
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
