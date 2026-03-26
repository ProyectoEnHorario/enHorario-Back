#!/usr/bin/env bash

set -u

BASE_URL="${BASE_URL:-http://localhost:8080/api/v1}"
ESTABLISHMENT_ID="${ESTABLISHMENT_ID:-}"
LOG_DIR="${LOG_DIR:-./logs}"
TS="$(date +%Y%m%d_%H%M%S)"
LOG_FILE="$LOG_DIR/backend_crud_check_$TS.log"

mkdir -p "$LOG_DIR"

PASS_COUNT=0
FAIL_COUNT=0
SKIP_COUNT=0

log() {
  local msg="$1"
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $msg" | tee -a "$LOG_FILE"
}

pass() {
  PASS_COUNT=$((PASS_COUNT + 1))
  log "PASS: $1"
}

fail() {
  FAIL_COUNT=$((FAIL_COUNT + 1))
  log "FAIL: $1"
}

skip() {
  SKIP_COUNT=$((SKIP_COUNT + 1))
  log "SKIP: $1"
}

extract_first_uuid() {
  echo "$1" | grep -Eo '[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}' | head -n 1
}

request() {
  local name="$1"
  local method="$2"
  local endpoint="$3"
  local expected_status="$4"
  local auth_value="${5:-}"
  local payload="${6:-}"

  local url="$BASE_URL$endpoint"
  local body_file
  body_file="$(mktemp)"

  local status
  if [[ -n "$auth_value" && -n "$payload" ]]; then
    status=$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $auth_value" \
      -d "$payload")
  elif [[ -n "$auth_value" ]]; then
    status=$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url" \
      -H "Authorization: Bearer $auth_value")
  elif [[ -n "$payload" ]]; then
    status=$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url" \
      -H "Content-Type: application/json" \
      -d "$payload")
  else
    status=$(curl -sS -o "$body_file" -w "%{http_code}" -X "$method" "$url")
  fi

  local body
  body="$(cat "$body_file")"
  rm -f "$body_file"

  log "REQUEST: $name"
  log "  $method $url"
  if [[ -n "$payload" ]]; then
    log "  payload: $payload"
  fi
  if [[ -n "$auth_value" ]]; then
    log "  auth: Bearer <redacted>"
  fi
  log "  status: $status"
  if [[ -n "$body" ]]; then
    log "  body: $body"
  fi

  if [[ "$status" == "$expected_status" ]]; then
    pass "$name (status $status)"
  else
    fail "$name (esperado $expected_status, recibido $status)"
  fi

  RESPONSE_STATUS="$status"
  RESPONSE_BODY="$body"
}

main() {
  log "== Iniciando verificacion backend con curl =="
  log "BASE_URL=$BASE_URL"
  if [[ -n "$ESTABLISHMENT_ID" ]]; then
    log "ESTABLISHMENT_ID (override)=$ESTABLISHMENT_ID"
  fi
  log "Nota: el API actual no expone CRUD completo para todas las entidades."
  log "- Establishments: solo lectura"
  log "- Auth: register/login/me (sin update/delete/list general)"
  log "- Turns: create/list/get/update/cancel"

  request "health" "GET" "/health" "200"

  request "establishments list" "GET" "/establishments?page=0&size=10" "200"
  local establishment_id
  if [[ -n "$ESTABLISHMENT_ID" ]]; then
    establishment_id="$ESTABLISHMENT_ID"
    pass "Usando establishmentId enviado por variable de entorno"
  else
    establishment_id="$(extract_first_uuid "$RESPONSE_BODY")"
    if [[ -z "$establishment_id" ]]; then
      skip "No se pudo extraer establishmentId del listado; se omiten pruebas de turns"
    else
      pass "Se extrajo establishmentId: $establishment_id"
    fi
  fi

  if [[ -n "$establishment_id" ]]; then
    request "establishments getById" "GET" "/establishments/$establishment_id" "200"
  fi
  request "establishments search" "GET" "/establishments/search?query=a&page=0&size=5" "200"
  request "establishments trending" "GET" "/establishments/trending/shortest-wait" "200"

  local suffix
  suffix="$(date +%s)"
  local email1="qa+$suffix-1@enhorario.com"
  local email2="qa+$suffix-2@enhorario.com"
  local passw="test1234"

  request "auth register user1" "POST" "/auth/register" "200" "" \
    "{\"name\":\"QA\",\"lastName\":\"Uno\",\"email\":\"$email1\",\"phone\":\"3000000001\",\"password\":\"$passw\"}"
  local user1_id
  user1_id="$(extract_first_uuid "$RESPONSE_BODY")"
  if [[ -z "$user1_id" ]]; then
    fail "No se pudo extraer user1_id de register user1"
    summary_and_exit
  else
    pass "Se extrajo user1_id: $user1_id"
  fi

  request "auth register user2" "POST" "/auth/register" "200" "" \
    "{\"name\":\"QA\",\"lastName\":\"Dos\",\"email\":\"$email2\",\"phone\":\"3000000002\",\"password\":\"$passw\"}"

  request "auth login user1" "POST" "/auth/login" "200" "" \
    "{\"email\":\"$email1\",\"password\":\"$passw\"}"

  if [[ -z "$establishment_id" ]]; then
    skip "Se omite bloque turns por ausencia de establishmentId"
    summary_and_exit
  fi

  request "turn create #1" "POST" "/turns" "200" "$user1_id" \
    "{\"establishmentId\":\"$establishment_id\",\"turnType\":\"REGULAR\"}"
  local turn1_id
  turn1_id="$(extract_first_uuid "$RESPONSE_BODY")"
  if [[ -z "$turn1_id" ]]; then
    fail "No se pudo extraer turn1_id"
    summary_and_exit
  else
    pass "Se extrajo turn1_id: $turn1_id"
  fi

  request "turn create #2" "POST" "/turns" "200" "$user1_id" \
    "{\"establishmentId\":\"$establishment_id\",\"turnType\":\"REGULAR\"}"
  local turn2_id
  turn2_id="$(extract_first_uuid "$RESPONSE_BODY")"
  if [[ -z "$turn2_id" ]]; then
    fail "No se pudo extraer turn2_id"
    summary_and_exit
  else
    pass "Se extrajo turn2_id: $turn2_id"
  fi

  request "turn list my-turns" "GET" "/turns/my-turns" "200" "$user1_id"
  request "turn getById turn1" "GET" "/turns/$turn1_id" "200"
  request "turn update status CALLED" "PUT" "/turns/$turn1_id/status?status=CALLED" "200"
  request "turn update status ATTENDED" "PUT" "/turns/$turn1_id/status?status=ATTENDED" "200"
  request "turn cancel turn2" "DELETE" "/turns/$turn2_id/cancel" "200" "$user1_id"

  request "turn getById turn2 after cancel" "GET" "/turns/$turn2_id" "200"
  if echo "$RESPONSE_BODY" | grep -q '"status":"CANCELLED"'; then
    pass "turn2 quedo en estado CANCELLED"
  else
    fail "turn2 no quedo CANCELLED en respuesta final"
  fi

  summary_and_exit
}

summary_and_exit() {
  log "== Resumen =="
  log "PASS=$PASS_COUNT"
  log "FAIL=$FAIL_COUNT"
  log "SKIP=$SKIP_COUNT"
  log "LOG_FILE=$LOG_FILE"

  if [[ "$FAIL_COUNT" -gt 0 ]]; then
    log "Resultado final: FALLA"
    exit 1
  fi

  log "Resultado final: OK"
  exit 0
}

main "$@"
