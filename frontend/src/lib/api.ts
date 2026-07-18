export const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

const ABSOLUTE_URL_PATTERN = /^[a-z][a-z\d+\-.]*:/i;
const API_BASE_CLEAN = API_BASE.replace(/\/+$/, "");
const API_ROOT = API_BASE_CLEAN.replace(/\/api$/, "");

function withoutApiPrefix(path: string): string {
  return path.startsWith("/api/") ? path.slice(4) : path;
}

export function resolveApiAssetUrl(url?: string | null): string {
  if (!url) return "";
  if (ABSOLUTE_URL_PATTERN.test(url)) return url;
  if (url.startsWith("/api/")) return `${API_ROOT}${url}`;
  if (url.startsWith("/")) return `${API_BASE_CLEAN}${url}`;
  return `${API_BASE_CLEAN}/${url}`;
}

export function toApiAssetPath(url?: string | null): string | undefined {
  if (!url) return undefined;
  if (url.startsWith(API_BASE_CLEAN)) return withoutApiPrefix(url.slice(API_ROOT.length));
  if (url.startsWith(API_ROOT)) return withoutApiPrefix(url.slice(API_ROOT.length));
  if (url.startsWith("/")) return withoutApiPrefix(url);
  return url;
}

export interface Noticia {
  id?: number;
  titulo: string;
  slug?: string;
  contenido: string;
  imagenPortada?: string;
  fechaPublicacion?: string;
  activo?: boolean;
}

export interface Carrusel {
  id?: number;
  titulo: string;
  imagenUrl: string;
  linkUrl?: string;
  orden?: number;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...options?.headers },
    ...options,
  });
  if (!res.ok) throw new Error(`API error: ${res.status} ${res.statusText}`);
  if (res.status === 204) return undefined as T;
  return res.json();
}

export async function checkHealth(): Promise<boolean> {
  try {
    const res = await fetch(`${API_BASE}/health`, { method: "GET", signal: AbortSignal.timeout(5000) });
    return res.ok;
  } catch {
    return false;
  }
}

export const noticiasApi = {
  list: () => request<Noticia[]>("/noticias"),
  getBySlug: (slug: string) => request<Noticia>(`/noticias/${slug}`),
  create: (data: Omit<Noticia, "id">) =>
    request<Noticia>("/noticias", { method: "POST", body: JSON.stringify(data) }),
  update: (slug: string, data: Partial<Noticia>) =>
    request<Noticia>(`/noticias/${slug}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (slug: string) =>
    request<void>(`/noticias/${slug}`, { method: "DELETE" }),
};

export const carruselApi = {
  list: () => request<Carrusel[]>("/carrusel"),
  get: (id: number) => request<Carrusel>(`/carrusel/${id}`),
  create: (formData: FormData) =>
    uploadRequest<Carrusel>("/carrusel", formData),
  update: (id: number, formData: FormData) =>
    uploadPutRequest<Carrusel>(`/carrusel/${id}`, formData),
  delete: (id: number) =>
    request<void>(`/carrusel/${id}`, { method: "DELETE" }),
  reordenar: (ids: number[]) =>
    request<Carrusel[]>("/carrusel/reordenar", {
      method: "PUT",
      body: JSON.stringify(ids),
    }),
};

export interface PDFDocumento {
  id?: number;
  nombre: string;
  nombreOriginal?: string;
  rutaArchivo?: string;
  url?: string;
  orden?: number;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

async function uploadRequest<T>(path: string, formData: FormData): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    body: formData,
  });
  if (!res.ok) throw new Error(`API error: ${res.status} ${res.statusText}`);
  return res.json();
}

async function uploadPutRequest<T>(path: string, formData: FormData): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "PUT",
    body: formData,
  });
  if (!res.ok) throw new Error(`API error: ${res.status} ${res.statusText}`);
  return res.json();
}

export const pdfApi = {
  list: () => request<PDFDocumento[]>("/pdf-documentos"),
  get: (id: number) => request<PDFDocumento>(`/pdf-documentos/${id}`),
  create: (formData: FormData) =>
    uploadRequest<PDFDocumento>("/pdf-documentos", formData),
  update: (id: number, formData: FormData) =>
    uploadPutRequest<PDFDocumento>(`/pdf-documentos/${id}`, formData),
  delete: (id: number) =>
    request<void>(`/pdf-documentos/${id}`, { method: "DELETE" }),
  reordenar: (ids: number[]) =>
    request<PDFDocumento[]>("/pdf-documentos/reordenar", {
      method: "PUT",
      body: JSON.stringify(ids),
    }),
};
