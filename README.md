# ControlPagina

Panel de control administrativo para la página web de **Coodmilla**.  
Maneja un CRUD de Noticias, Documentos PDF (DIAN-ESAL) e Imágenes del carrusel.

## Requisitos

- **Java 17** o superior
- **Node.js 18** o superior
- **MySQL 8** corriendo en `localhost:3306`

## Backend (Spring Boot + Java)

```bash
# 1. Asegúrate de tener MySQL corriendo con la base de datos:
#    CREATE DATABASE PaginaCod;
#
# 2. Las credenciales por defecto son admin / admin
#    (se pueden sobreescribir con DB_USER y DB_PASS)

cd backend
# Usa el wrapper (mvnw) — no requiere Maven instalado
.\mvnw spring-boot:run
```

El backend arranca en `http://localhost:8080`.  
Los endpoints REST están bajo `/api/`.

## Frontend (Next.js + Tailwind + shadcn/ui)

```bash
cd frontend
pnpm install
pnpm run dev
```

El frontend arranca en `http://localhost:3000`.

## Variables de entorno (Frontend)

Crea un archivo `.env.local` en `frontend/` si el backend corre en otro puerto:

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Rutas del frontend

| Ruta          | Descripción                    |
| ------------- | ------------------------------ |
| `/`           | Dashboard / inicio             |
| `/noticias`   | CRUD de noticias               |
| `/documentos` | CRUD de documentos DIAN-ESAL   |
| `/carrusel`   | CRUD de imágenes del carrusel  |
