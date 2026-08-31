# ControlPagina

Panel de control administrativo para la página web de **Coodmilla**.  
Maneja un CRUD de Noticias, Documentos PDF (DIAN-ESAL), Imágenes del carrusel y Trabajadores.

## Requisitos

- **Java 17** o superior
- **Node.js 18** o superior
- **MySQL 8** corriendo en `localhost:3306`

## Backend (Spring Boot + Java)

```bash
# 1. MySQL con la base:
#    CREATE DATABASE PaginaCod;
#
# 2. Credenciales DB por defecto: root / admin (sobrescribir con DB_USER / DB_PASS)
# 3. Credenciales del panel CMS: ADMIN_USER / ADMIN_PASSWORD (ver .env.example)

cd backend
.\mvnw spring-boot:run
```

El backend arranca en `http://localhost:8080`.  
Los endpoints REST viven bajo `/api/`.

### Seguridad API

| Ruta | Auth |
|------|------|
| `GET /api/publico/**`, `GET /api/health`, `GET /archivos/**` | Pública |
| `POST /api/auth/login` | Pública (valida credenciales) |
| Resto del CRUD (`/api/noticias`, uploads, etc.) | HTTP Basic (`ADMIN_USER` / `ADMIN_PASSWORD`) |

## Frontend (Next.js + Tailwind + shadcn/ui)

```bash
cd frontend
pnpm install
pnpm run dev
```

El panel arranca en `http://localhost:3001`.

Ruta `/login`: pantalla de acceso. Tras autenticar, una cookie `httpOnly` guarda la sesión y el BFF (`/api/proxy/*`) reenvía las peticiones al backend con Basic Auth. El middleware protege todas las rutas del panel.

## Variables de entorno

### Backend (`backend/.env.example`)

```
ADMIN_USER=admin
ADMIN_PASSWORD=cambia-esta-clave-segura
DB_URL=...
DB_USER=...
DB_PASS=...
CORS_ORIGINS=http://localhost:3000,http://localhost:3001
```

### Frontend (`frontend/.env.local`)

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Rutas del frontend

| Ruta | Descripción |
|------|-------------|
| `/login` | Inicio de sesión |
| `/` | Dashboard |
| `/noticias` | CRUD de noticias |
| `/documentos` | CRUD de documentos DIAN-ESAL |
| `/carrusel` | CRUD de imágenes del carrusel |
| `/trabajadores` | CRUD de trabajadores |

Ver también `DEPLOY.md` en la raíz del monorepo.
