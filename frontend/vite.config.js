import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// En desarrollo, Vite actúa como API Gateway enrutando cada prefijo a su microservicio.
// En Docker, este papel lo cumple nginx (ver nginx.conf).
const USER_SERVICE = process.env.USER_SERVICE_URL ?? 'http://localhost:8081';
const CATALOG_SERVICE = process.env.CATALOG_SERVICE_URL ?? 'http://localhost:8082';
const ORDER_SERVICE = process.env.ORDER_SERVICE_URL ?? 'http://localhost:8083';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/auth': USER_SERVICE,
      '/api/users': USER_SERVICE,
      '/api/products': CATALOG_SERVICE,
      '/api/cart': ORDER_SERVICE,
      '/api/orders': ORDER_SERVICE,
      '/api/admin': ORDER_SERVICE,
    },
  },
  build: {
    sourcemap: false,
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    css: false,
  },
});
