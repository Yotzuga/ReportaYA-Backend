#!/bin/sh
# Arma la configuracion de base de datos que espera Spring a partir de lo que
# entregue el proveedor, sin escribir contrasenas en ningun archivo:
#  - SPRING_DATASOURCE_URL/USERNAME/PASSWORD ya definidas -> se usan tal cual.
#  - DATABASE_URL=postgres(ql)://usuario:clave@host[:puerto]/base[?params] (Neon, Render, etc.)
#  - DB_HOST, DB_PORT, DB_NAME (+ SPRING_DATASOURCE_USERNAME/PASSWORD), como en render.yaml.
set -e

if [ -z "$SPRING_DATASOURCE_URL" ] && [ -n "$DATABASE_URL" ]; then
  resto="${DATABASE_URL#*://}"
  credenciales="${resto%%@*}"
  servidor="${resto#*@}"
  export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${credenciales%%:*}}"
  export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${credenciales#*:}}"
  case "$servidor" in
    *\?*) export SPRING_DATASOURCE_URL="jdbc:postgresql://${servidor}" ;;
    *)    export SPRING_DATASOURCE_URL="jdbc:postgresql://${servidor}?sslmode=${DB_SSLMODE:-prefer}" ;;
  esac
fi

if [ -z "$SPRING_DATASOURCE_URL" ] && [ -n "$DB_HOST" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME:-postgres}?sslmode=${DB_SSLMODE:-prefer}"
fi

# Render publica la URL del servicio; se usa para los enlaces de los correos.
if [ -z "$APP_BASE_URL" ] && [ -n "$RENDER_EXTERNAL_URL" ]; then
  export APP_BASE_URL="$RENDER_EXTERNAL_URL"
fi

exec java -jar /app/app.jar
