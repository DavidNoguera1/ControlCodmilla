import {
  Newspaper,
  FileText,
  Images,
  Activity,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const stats = [
  {
    title: "Noticias",
    value: "—",
    icon: Newspaper,
    href: "/noticias",
    color: "text-brand-green",
    bg: "bg-brand-green/10",
  },
  {
    title: "Documentos DIAN-ESAL",
    value: "—",
    icon: FileText,
    href: "/documentos",
    color: "text-brand-gold",
    bg: "bg-brand-gold/10",
  },
  {
    title: "Imágenes Carrusel",
    value: "—",
    icon: Images,
    href: "/carrusel",
    color: "text-brand-green-light",
    bg: "bg-brand-green-light/10",
  },
];

export default function Dashboard() {
  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-brand-dark">
          Panel de Control
        </h1>
        <p className="text-brand-text-muted mt-1 text-sm">
          Administra el contenido de la página de Coodmilla
        </p>
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {stats.map((s) => (
          <a key={s.title} href={s.href}>
            <Card className="transition-all hover:shadow-lg hover:-translate-y-0.5 border-brand-border">
              <CardHeader className="flex flex-row items-center gap-4 pb-2">
                <div className={`rounded-lg p-2.5 ${s.bg}`}>
                  <s.icon className={`h-5 w-5 ${s.color}`} />
                </div>
                <CardTitle className="text-sm font-medium text-brand-text-muted">
                  {s.title}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-3xl font-bold text-brand-dark">{s.value}</p>
              </CardContent>
            </Card>
          </a>
        ))}
      </div>

      <Card className="border-brand-border">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Activity className="h-4 w-4 text-brand-green" />
            Actividad reciente
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-brand-text-muted">
            Conéctate a la base de datos para ver la actividad reciente.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
