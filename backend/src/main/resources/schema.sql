DROP TABLE IF EXISTS noticias;

CREATE TABLE noticias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    slug VARCHAR(500) NOT NULL UNIQUE,
    contenido LONGTEXT,
    imagen_portada VARCHAR(500),
    fecha_publicacion DATETIME,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_activo (activo),
    INDEX idx_slug (slug),
    INDEX idx_fecha_publicacion (fecha_publicacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
