#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL_FILE="$ROOT_DIR/scripts/railway_seed_dev.sql"
LOG_DIR="${LOG_DIR:-$ROOT_DIR/logs}"
TS="$(date +%Y%m%d_%H%M%S)"
LOG_FILE="$LOG_DIR/railway_seed_dev_$TS.log"

mkdir -p "$LOG_DIR"

if ! command -v psql >/dev/null 2>&1; then
  echo "ERROR: psql no esta instalado o no esta en PATH." | tee -a "$LOG_FILE"
  echo "Instala cliente postgres y vuelve a ejecutar." | tee -a "$LOG_FILE"
  exit 1
fi

if [[ -z "${DATABASE_URL:-}" ]]; then
  echo "ERROR: DATABASE_URL no esta definido." | tee -a "$LOG_FILE"
  echo "Ejemplo: DATABASE_URL='postgresql://user:pass@host:port/db?sslmode=require' ./scripts/seed_railway_dev.sh" | tee -a "$LOG_FILE"
  exit 1
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Iniciando seed de Railway" | tee -a "$LOG_FILE"
echo "SQL_FILE=$SQL_FILE" | tee -a "$LOG_FILE"

psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f "$SQL_FILE" | tee -a "$LOG_FILE"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Seed finalizado correctamente" | tee -a "$LOG_FILE"
echo "LOG_FILE=$LOG_FILE" | tee -a "$LOG_FILE"

echo
echo "Credenciales seed para pruebas:"
echo "- admin.dev@enhorario.com / test1234"
echo "- usuario.dev@enhorario.com / test1234"
echo "Establishment principal para pruebas CRUD de turns:"
echo "- 22222222-2222-2222-2222-222222222222"
