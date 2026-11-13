#!/usr/bin/env bash

# ==========================================
# CONFIGURACIÓN DE PARÁMETROS
# ==========================================
KEYCLOAK_URL="http://localhost:8080"
REALM="pedidos360"
CLIENT_ID="pedidos360"

ADMIN_USER="admin"
ADMIN_PASS="12345"

MS_ADMIN_URL="http://localhost:8085"

# Nombres de recursos de prueba
TEST_QUEUE="test.admin.queue"
TEST_EXCHANGE="test.admin.exchange"
TEST_ROUTING_KEY="test.admin.key"

# Colores para la salida
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${YELLOW}====================================================${NC}"
echo -e "${YELLOW}   PRUEBA DE ENDPOINTS - MS-RABBITMQ-ADMIN          ${NC}"
echo -e "${YELLOW}====================================================${NC}\n"

# 1. Verificar dependencias
if ! command -v jq &> /dev/null; then
    echo -e "${RED}[ERROR] 'jq' no está instalado.${NC}"
    exit 1
fi

# ==========================================
# PASO 1: AUTENTICACIÓN CON KEYCLOAK
# ==========================================
echo -e "${GREEN}[1/5] Obteniendo token de Administrador desde Keycloak...${NC}"
TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${ADMIN_USER}" \
  -d "password=${ADMIN_PASS}" | jq -r '.access_token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo -e "${RED}[FALLO] Error al obtener el token JWT para el usuario administrador.${NC}"
    exit 1
fi
echo -e "   Token JWT obtenido con éxito.\n"

# Función auxiliar para validar respuestas HTTP
assert_status() {
    local label=$1
    local expected=$2
    local actual=$3

    if [ "$actual" -eq "$expected" ]; then
        echo -e "   ${GREEN}✔ [${actual}] ${label}${NC}"
    else
        echo -e "   ${RED}✘ [${actual}] ${label} (Esperado: ${expected})${NC}"
    fi
}

# ==========================================
# PASO 2: PRUEBAS DE GESTIÓN DE COLAS
# ==========================================
echo -e "${CYAN}----------------------------------------------------${NC}"
echo -e "${CYAN} 1. GESTIÓN DE COLAS                                ${NC}"
echo -e "${CYAN}----------------------------------------------------${NC}"

# Crear Cola
STATUS_CREATE_Q=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${MS_ADMIN_URL}/api/v1/admin/queues" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"${TEST_QUEUE}\",
    \"durable\": true
  }")
assert_status "Crear Cola ('${TEST_QUEUE}')" 201 "$STATUS_CREATE_Q"

# ==========================================
# PASO 3: PRUEBAS DE GESTIÓN DE EXCHANGES
# ==========================================
echo -e "\n${CYAN}----------------------------------------------------${NC}"
echo -e "${CYAN} 2. GESTIÓN DE EXCHANGES                            ${NC}"
echo -e "${CYAN}----------------------------------------------------${NC}"

# Crear Exchange
STATUS_CREATE_EX=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${MS_ADMIN_URL}/api/v1/admin/exchanges" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"${TEST_EXCHANGE}\",
    \"type\": \"topic\",
    \"durable\": true
  }")
assert_status "Crear Exchange ('${TEST_EXCHANGE}' - topic)" 201 "$STATUS_CREATE_EX"

# ==========================================
# PASO 4: PRUEBAS DE GESTIÓN DE BINDINGS
# ==========================================
echo -e "\n${CYAN}----------------------------------------------------${NC}"
echo -e "${CYAN} 3. GESTIÓN DE BINDINGS                             ${NC}"
echo -e "${CYAN}----------------------------------------------------${NC}"

# Crear Binding
STATUS_CREATE_BIND=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${MS_ADMIN_URL}/api/v1/admin/bindings" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"destinationQueue\": \"${TEST_QUEUE}\",
    \"exchange\": \"${TEST_EXCHANGE}\",
    \"routingKey\": \"${TEST_ROUTING_KEY}\"
  }")
assert_status "Crear Binding (${TEST_QUEUE} <-> ${TEST_EXCHANGE})" 201 "$STATUS_CREATE_BIND"

# Eliminar Binding
STATUS_DELETE_BIND=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${MS_ADMIN_URL}/api/v1/admin/bindings" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"destinationQueue\": \"${TEST_QUEUE}\",
    \"exchange\": \"${TEST_EXCHANGE}\",
    \"routingKey\": \"${TEST_ROUTING_KEY}\"
  }")
assert_status "Eliminar Binding" 204 "$STATUS_DELETE_BIND"

# ==========================================
# PASO 5: LIMPIEZA DE RECURSOS (DELETE)
# ==========================================
echo -e "\n${CYAN}----------------------------------------------------${NC}"
echo -e "${CYAN} 4. ELIMINACIÓN DE RECURSOS                         ${NC}"
echo -e "${CYAN}----------------------------------------------------${NC}"

# Eliminar Cola
STATUS_DELETE_Q=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${MS_ADMIN_URL}/api/v1/admin/queues/${TEST_QUEUE}" \
  -H "Authorization: Bearer ${TOKEN}")
assert_status "Eliminar Cola ('${TEST_QUEUE}')" 204 "$STATUS_DELETE_Q"

# Eliminar Exchange
STATUS_DELETE_EX=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${MS_ADMIN_URL}/api/v1/admin/exchanges/${TEST_EXCHANGE}" \
  -H "Authorization: Bearer ${TOKEN}")
assert_status "Eliminar Exchange ('${TEST_EXCHANGE}')" 204 "$STATUS_DELETE_EX"

# ==========================================
# PASO 6: PRUEBAS DE VALIDACIÓN (@Valid)
# ==========================================
echo -e "\n${CYAN}----------------------------------------------------${NC}"
echo -e "${CYAN} 5. VALIDACIÓN DE ERRORES (@Valid DTOs)             ${NC}"
echo -e "${CYAN}----------------------------------------------------${NC}"

# Probar creación de cola con nombre vacío
STATUS_INVALID_Q=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${MS_ADMIN_URL}/api/v1/admin/queues" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"\",
    \"durable\": true
  }")
assert_status "Rechazar Cola con nombre vacío (400)" 400 "$STATUS_INVALID_Q"

# Probar creación de exchange con tipo inválido
STATUS_INVALID_EX=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${MS_ADMIN_URL}/api/v1/admin/exchanges" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"invalid.exchange\",
    \"type\": \"invalid_type\",
    \"durable\": true
  }")
assert_status "Rechazar Exchange con tipo inválido (400)" 400 "$STATUS_INVALID_EX"

echo -e "\n${YELLOW}====================================================${NC}"
echo -e "${YELLOW}   PRUEBAS FINALIZADAS                             ${NC}"
echo -e "${YELLOW}====================================================${NC}"