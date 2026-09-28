# syntax=docker/dockerfile:1

# ---- Etapa 1: build de la SPA con Vite ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# URL pública de Mailpit (solo entorno de demostración) para mostrarla en "Recuperar contraseña".
ARG VITE_MAILPIT_URL=""
ENV VITE_MAILPIT_URL=${VITE_MAILPIT_URL}
RUN npm run build

# ---- Etapa 2: nginx sirve los estáticos y actúa como API Gateway ----
FROM nginx:1.29-alpine
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s --retries=3 CMD wget -qO- http://127.0.0.1/healthz || exit 1
