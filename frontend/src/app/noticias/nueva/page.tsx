"use client";

import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Save, ImagePlus, X, Star } from "lucide-react";
import Link from "next/link";
import dynamic from "next/dynamic";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { noticiasApi } from "@/lib/api";
import type { EditorHandle } from "@/components/EditorWrapper";

const EditorWrapper = dynamic(() => import("@/components/EditorWrapper"), { ssr: false });

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

function generarSlug(texto: string): string {
  return texto
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, "")
    .replace(/[\s-]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

async function uploadFile(file: File, tipo?: string): Promise<string> {
  const formData = new FormData();
  formData.append("file", file);
  if (tipo) formData.append("tipo", tipo);
  const res = await fetch(`${API_BASE}/archivos`, { method: "POST", body: formData });
  const data = await res.json();
  if (!data.success) throw new Error("Error al subir archivo");
  return data.file.url;
}

export default function NuevaNoticiaPage() {
  const router = useRouter();
  const editorRef = useRef<EditorHandle>(null);
  const portadaFileRef = useRef<File | null>(null);
  const [titulo, setTitulo] = useState("");
  const [slug, setSlug] = useState("");
  const [activo, setActivo] = useState(true);
  const [destacado, setDestacado] = useState(false);
  const [imagenPortada, setImagenPortada] = useState<string | null>(null);
  const [portadaPreview, setPortadaPreview] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const handleTituloChange = (v: string) => {
    setTitulo(v);
    setSlug(generarSlug(v));
  };

  const handlePortadaUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    portadaFileRef.current = file;
    setImagenPortada(null);
    setPortadaPreview(URL.createObjectURL(file));
  };

  const removePortada = () => {
    portadaFileRef.current = null;
    setImagenPortada(null);
    setPortadaPreview(null);
  };

  const handleSave = async () => {
    if (!titulo.trim()) { toast.error("El título es obligatorio"); return; }

    setSaving(true);
    try {
      const output = await editorRef.current?.getData();
      const blocks = output?.blocks || [];
      const pendingFiles = editorRef.current?.getPendingFiles() || new Map();

      for (const block of blocks) {
        if (block.type === "image" && block.data?.file?.tempId) {
          const tempId = block.data.file.tempId;
          const file = pendingFiles.get(tempId);
          if (file) {
            const serverUrl = await uploadFile(file, "imagenesNoticias");
            block.data.file.url = serverUrl;
            delete block.data.file.tempId;
          }
        }
      }

      let portadaUrl = imagenPortada;
      if (portadaFileRef.current) {
        portadaUrl = await uploadFile(portadaFileRef.current, "imagenesNoticias");
        portadaFileRef.current = null;
      }

      const contenido = { time: Date.now(), blocks, version: "2.31.6" };

      await noticiasApi.create({
        titulo: titulo.trim(),
        slug: slug || generarSlug(titulo),
        contenido: JSON.stringify(contenido),
        imagenPortada: portadaUrl || undefined,
        activo,
        destacado,
      });
      toast.success("Noticia creada correctamente");
      router.push("/noticias");
    } catch {
      toast.error("Error al crear la noticia");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex items-center gap-4 mb-8">
        <Link
          href="/noticias"
          className="flex items-center justify-center w-10 h-10 rounded-xl border border-brand-border text-brand-text-muted hover:text-brand-dark hover:bg-brand-cream transition-all shrink-0"
        >
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-brand-dark">Nueva noticia</h1>
          <p className="text-sm text-brand-text-muted mt-0.5">Crea contenido con el editor enriquecido</p>
        </div>
      </div>

      <div className="space-y-6">
        <div className="rounded-xl border border-brand-border bg-white shadow-sm overflow-hidden">
          <div className="px-8 py-5 border-b border-brand-border/50 bg-brand-cream/20">
            <h2 className="text-sm font-semibold text-brand-dark tracking-wide uppercase">Información</h2>
          </div>
          <div className="px-8 py-6 space-y-5">
            <div className="space-y-1.5">
              <Label htmlFor="titulo" className="text-sm font-semibold text-brand-text">Título</Label>
              <Input
                id="titulo"
                name="titulo"
                value={titulo}
                onChange={(e) => handleTituloChange(e.target.value)}
                placeholder="Ej: Coodmilla inaugura nuevo centro de acopio"
                className="border-brand-border text-xl font-semibold h-14 px-5"
              />
              {slug && (
                <p className="text-xs text-brand-text-muted mt-1.5 flex items-center gap-1.5">
                  <span className="inline-block w-1.5 h-1.5 rounded-full bg-brand-green/60" />
                  URL: <span className="font-mono">/noticias/{slug}</span>
                </p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label className="text-sm font-semibold text-brand-text">Imagen de portada</Label>
              {portadaPreview ? (
                <div className="relative rounded-xl overflow-hidden border border-brand-border bg-brand-cream">
                  <img src={portadaPreview} alt="Portada" className="w-full h-56 object-cover" />
                  <button
                    type="button"
                    onClick={removePortada}
                    className="absolute top-3 right-3 bg-white/90 backdrop-blur-sm rounded-full p-1.5 shadow hover:bg-white transition-colors"
                  >
                    <X className="h-4 w-4 text-red-500" />
                  </button>
                </div>
              ) : (
                <label
                  htmlFor="portada-upload"
                  className="flex flex-col items-center justify-center h-40 rounded-xl border-2 border-dashed border-brand-border bg-brand-cream/30 hover:bg-brand-cream/60 hover:border-brand-green/40 cursor-pointer transition-all group"
                >
                  <div className="flex flex-col items-center gap-1.5 text-brand-text-muted group-hover:text-brand-green">
                    <ImagePlus className="h-8 w-8" />
                    <span className="text-sm font-medium">Imagen de portada</span>
                    <span className="text-xs">PNG, JPG o WebP</span>
                  </div>
                </label>
              )}
              <input
                id="portada-upload"
                name="portada-upload"
                type="file"
                accept="image/png,image/jpeg,image/webp"
                onChange={handlePortadaUpload}
                className="sr-only"
              />
            </div>
          </div>
        </div>

        <div className="rounded-xl border border-brand-border bg-white shadow-sm">
          <div className="px-8 py-5 border-b border-brand-border/50 bg-brand-cream/20 flex items-center justify-between">
            <h2 className="text-sm font-semibold text-brand-dark tracking-wide uppercase">Contenido</h2>
            <span className="text-xs text-brand-text-muted">Editor enriquecido</span>
          </div>
          <div className="p-6">
            <div className="rounded-lg border border-brand-border bg-white min-h-[500px]">
              <EditorWrapper ref={editorRef} holder="editor-nueva" />
            </div>
          </div>
          <div className="px-8 py-5 border-t border-brand-border/50 bg-brand-cream/20 flex items-center justify-between gap-4 flex-wrap">
            <div className="flex items-center gap-3">
              <span className="text-sm text-brand-text-muted">Estado:</span>
              <button
                type="button"
                role="switch"
                aria-checked={activo}
                onClick={() => setActivo(!activo)}
                className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors ${
                  activo ? "bg-brand-green" : "bg-brand-border"
                }`}
              >
                <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow-sm transition-transform ${
                  activo ? "translate-x-[22px]" : "translate-x-[2px]"
                }`} />
              </button>
              <span className={`text-sm font-medium ${activo ? "text-brand-green" : "text-brand-text-muted"}`}>
                {activo ? "Publicado" : "Borrador"}
              </span>
              <button
                type="button"
                aria-pressed={destacado}
                onClick={() => setDestacado(!destacado)}
                className={`ml-2 inline-flex h-9 items-center gap-2 rounded-lg border px-3 text-sm font-semibold transition-colors ${
                  destacado
                    ? "border-brand-gold/40 bg-brand-gold/15 text-brand-gold"
                    : "border-brand-border bg-white text-brand-text-muted hover:text-brand-dark"
                }`}
              >
                <Star className={`h-4 w-4 ${destacado ? "fill-current" : ""}`} />
                Destacado
              </button>
            </div>
            <div className="flex items-center gap-3">
              <Link href="/noticias">
                <Button variant="outline" className="border-brand-border h-11">Cancelar</Button>
              </Link>
              <Button
                onClick={handleSave}
                disabled={saving || !titulo.trim()}
                className="bg-brand-green hover:bg-brand-green/90 text-white h-11 px-8"
              >
                {saving ? (
                  <span className="flex items-center gap-2">
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                    Guardando...
                  </span>
                ) : (
                  <span className="flex items-center gap-2"><Save className="h-4 w-4" /> Publicar</span>
                )}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
