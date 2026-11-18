# Despliegue en la nube (Render + Docker)

El backend se empaqueta con el `Dockerfile` de la raiz (compila con Maven y corre
solo con el JRE 17). Sirve para Render o para cualquier host que ejecute Docker.

## Variables de entorno

| Variable | Obligatoria | Para que |
|---|---|---|
| `JWT_SECRET` | si | Firma de los tokens. 32 bytes o mas. En Render: `generateValue: true`. |
| `DATABASE_URL` | si (o las tres `SPRING_DATASOURCE_*`) | `postgresql://usuario:clave@host[:puerto]/base[?sslmode=require]`. El `docker-entrypoint.sh` la convierte a JDBC. |
| `PORT` | no | Puerto HTTP. Render lo define solo. Por defecto 8081. |
| `APP_VERIFICAR_CORREO` | no | `false` = las cuentas nuevas nacen activas. Usar mientras no haya un dominio verificado en Resend (sin dominio, Resend solo entrega al dueno de la cuenta). |
| `APP_SEED_OPERADOR_HASH`, `APP_SEED_TECNICO_HASH` | no | Hashes BCrypt para crear `op.maria` y `tec.carlos`, `tec.lucia`, `tec.maria` en una base vacia. Nunca se guarda la clave en texto plano. |
| `RESEND_API_KEY`, `RESEND_FROM` | no | Correos reales (verificacion y codigo de recuperacion). |
| `FIREBASE_CREDENTIALS_BASE64`, `FIREBASE_STORAGE_BUCKET` | no | Fotos en Firebase en vez de PostgreSQL. |
| `APP_BASE_URL` | no | URL publica para los enlaces de los correos. En Render se toma de `RENDER_EXTERNAL_URL`. |

Para generar un hash BCrypt: `python3 -c "import bcrypt;print(bcrypt.hashpw(b'CLAVE', bcrypt.gensalt(10)).decode())"`.

## Chequeo de salud

`GET /salud` (sin token) responde `{"estado":"ok","base":"ok"}` si la app y la base
responden. Render lo usa para dar por bueno un despliegue y sirve para el ping que
evita que el plan gratis se duerma (Render apaga el servicio tras 15 min sin trafico).

## Render con Blueprint

Un `render.yaml` con un servicio `type: web`, `runtime: docker`, `plan: free`,
`healthCheckPath: /salud` y una base `plan: free` enlazada por
`fromDatabase: { property: connectionString }` en `DATABASE_URL` deja todo listo con
un clic (Render > New > Blueprint). La Postgres gratis de Render **vence a los 30
dias** de creada (14 dias de gracia para pasarla a un plan pago).

## Pasar la base a Neon (gratis sin vencimiento)

1. Crear un proyecto en Neon y copiar su cadena de conexion (`...?sslmode=require`).
2. En Render, en la base: *Networking* > permitir temporalmente tu IP y copiar la
   *External Database URL*.
3. Copiar los datos: `pg_dump --no-owner --no-acl "URL_EXTERNA_RENDER" | psql "URL_NEON"`.
4. En `render.yaml`, cambiar `DATABASE_URL` de `fromDatabase` a `sync: false`, subir el
   cambio y pegar la URL de Neon en *Environment* del servicio. Render redespliega.
5. Probar `GET /salud` y un login; luego borrar la base de Render.
