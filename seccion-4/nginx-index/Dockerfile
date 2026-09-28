# Contenedor con la última versión de nginx que sirve únicamente un index.html.
#   docker build -t nginx-index .
#   docker run --rm -p 8088:80 nginx-index   ->  http://localhost:8088
FROM nginx:latest

# Se eliminan los archivos por defecto para que el contenedor sirva solo nuestro index.html.
RUN rm -rf /usr/share/nginx/html/*
COPY index.html /usr/share/nginx/html/index.html

EXPOSE 80
# La imagen base ya define: CMD ["nginx", "-g", "daemon off;"]
