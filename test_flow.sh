#!/usr/bin/env bash

# ==========================================
# CONFIGURACIÓN DE PARÁMETROS Y CARGA
# ==========================================
KEYCLOAK_URL="http://localhost:8080"
REALM="pedidos360"
CLIENT_ID="pedidos360"

CLIENTE_USER="cliente"
CLIENTE_PASS="12345"

STAFF_USER="admin"
STAFF_PASS="12345"

MS_CATALOG_URL="http://localhost:8081"
MS_ORDERS_URL="http://localhost:8082"

# API REST de RabbitMQ para inspeccionar colas
RABBITMQ_API_URL="http://localhost:15672/api"
RABBITMQ_USER="admin"
RABBITMQ_PASS="admin123"
DLQ_NAME="catalog.order-created.dlq"

# Configuración de Pruebas de Carga
TOTAL_REQUESTS=30     # Total de hilos/escenarios a lanzar
CONCURRENCY=6         # Nivel de peticiones simultáneas
WAIT_DLQ_SECONDS=10    # Tiempo de espera para consumir reintentos de AMQP

RESULTS_FILE=$(mktemp)

# Colores de Consola
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${YELLOW}====================================================${NC}"
echo -e "${YELLOW}   PRUEBA DE CARGA Y GENERACIÓN DE EVENTOS EN DLQ   ${NC}"
echo -e "${YELLOW}====================================================${NC}"

if ! command -v jq &> /dev/null; then
    echo -e "${RED}[ERROR] 'jq' no está instalado.${NC}"
    exit 1
fi

# ==========================================
# PASO 1: AUTENTICACIÓN
# ==========================================
echo -e "\n${GREEN}[1/5] Autenticando usuarios en Keycloak...${NC}"
TOKEN_CLIENTE=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${CLIENTE_USER}" \
  -d "password=${CLIENTE_PASS}" | jq -r '.access_token')

TOKEN_STAFF=$(curl -s -X POST "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "username=${STAFF_USER}" \
  -d "password=${STAFF_PASS}" | jq -r '.access_token')

if [ "$TOKEN_CLIENTE" == "null" ] || [ "$TOKEN_STAFF" == "null" ]; then
    echo -e "${RED}[FALLO] No se pudieron obtener los tokens JWT.${NC}"
    exit 1
fi
echo -e "   Tokens JWT cargados."

# ==========================================
# PASO 2: PREPARAR PRODUCTOS DE PRUEBA
# ==========================================
SKU_LOAD="LOAD-DLQ-$(date +%s)"
STOCK_LIMITADO=10

echo -e "\n${GREEN}[2/5] Creando producto con stock acotado (${STOCK_LIMITADO} unidades)...${NC}"
CREATE_PROD_RES=$(curl -s -X POST "${MS_CATALOG_URL}/api/v1/products" \
  -H "Authorization: Bearer ${TOKEN_STAFF}" \
  -H "Content-Type: application/json" \
  -d "{
    \"sku\": \"${SKU_LOAD}\",
    \"name\": \"Producto Test Concurrencia y DLQ\",
    \"description\": \"Diseñado para generar fallos asíncronos\",
    \"price\": 10000.00,
    \"stock\": ${STOCK_LIMITADO}
  }")

PRODUCT_ID=$(echo "$CREATE_PROD_RES" | jq -r '.data.id')
INVALID_PRODUCT_ID="00000000-0000-0000-0000-000000000000"

echo -e "   Producto Válido ID   : ${PRODUCT_ID} | Stock: ${STOCK_LIMITADO}"
echo -e "   Producto Inválido ID : ${INVALID_PRODUCT_ID}"

export MS_CATALOG_URL MS_ORDERS_URL TOKEN_CLIENTE TOKEN_STAFF PRODUCT_ID INVALID_PRODUCT_ID RESULTS_FILE

# ==========================================
# PASO 3: TRABAJADOR SIMULTÁNEO CON CASOS DE FALLO
# ==========================================
run_mixed_worker() {
    local index=$1
    local mod=$((index % 3))

    if [ "$mod" -eq 0 ]; then
        # CASO A: FLUJO CORRECTO (Pide 1 unidad -> Stock OK -> Procesa evento correctamente)
        CREATE_RES=$(curl -s -X POST "${MS_ORDERS_URL}/api/v1/orders" \
          -H "Authorization: Bearer ${TOKEN_CLIENTE}" \
          -H "Content-Type: application/json" \
          -d "{\"items\": [{\"productId\": \"${PRODUCT_ID}\", \"quantity\": 1}]}")

        ORDER_ID=$(echo "$CREATE_RES" | jq -r '.data.id // empty')

        if [ -n "$ORDER_ID" ]; then
            curl -s -X PATCH "${MS_ORDERS_URL}/api/v1/orders/${ORDER_ID}/accept" \
              -H "Authorization: Bearer ${TOKEN_STAFF}" > /dev/null
            echo "OK_VALID_EVENT" >> "$RESULTS_FILE"
        else
            echo "FAIL_ORDER_CREATION" >> "$RESULTS_FILE"
        fi

    elif [ "$mod" -eq 1 ]; then
        # CASO B: CASO DE FALLO DLQ 1 - EXCESO DE STOCK EN CONSUMIDOR
        # Crea la orden pidiendo 9999 unidades. Se acepta en ms-orders, pero al ser consumida
        # en ms-catalog falla por InsufficientStockException, agota 3 reintentos y cae a DLQ.
        CREATE_RES=$(curl -s -X POST "${MS_ORDERS_URL}/api/v1/orders" \
          -H "Authorization: Bearer ${TOKEN_CLIENTE}" \
          -H "Content-Type: application/json" \
          -d "{\"items\": [{\"productId\": \"${PRODUCT_ID}\", \"quantity\": 9999}]}")

        ORDER_ID=$(echo "$CREATE_RES" | jq -r '.data.id // empty')

        if [ -n "$ORDER_ID" ]; then
            curl -s -X PATCH "${MS_ORDERS_URL}/api/v1/orders/${ORDER_ID}/accept" \
              -H "Authorization: Bearer ${TOKEN_STAFF}" > /dev/null
            echo "TRIGGERED_DLQ_EXCESS_STOCK" >> "$RESULTS_FILE"
        else
            echo "FAIL_ORDER_CREATION" >> "$RESULTS_FILE"
        fi

    else
        # CASO C: CASO DE FALLO DLQ 2 - PRODUCTO INEXISTENTE EN EVENTO
        # Pide un producto con UUID inexistente. Al procesar la orden aceptada en ms-catalog,
        # lanza ResourceNotFoundException, agota reintentos y deriva el evento a DLQ.
        CREATE_RES=$(curl -s -X POST "${MS_ORDERS_URL}/api/v1/orders" \
          -H "Authorization: Bearer ${TOKEN_CLIENTE}" \
          -H "Content-Type: application/json" \
          -d "{\"items\": [{\"productId\": \"${INVALID_PRODUCT_ID}\", \"quantity\": 1}]}")

        ORDER_ID=$(echo "$CREATE_RES" | jq -r '.data.id // empty')

        if [ -n "$ORDER_ID" ]; then
            curl -s -X PATCH "${MS_ORDERS_URL}/api/v1/orders/${ORDER_ID}/accept" \
              -H "Authorization: Bearer ${TOKEN_STAFF}" > /dev/null
            echo "TRIGGERED_DLQ_INVALID_PRODUCT" >> "$RESULTS_FILE"
        else
            echo "FAIL_ORDER_CREATION" >> "$RESULTS_FILE"
        fi
    fi
}

export -f run_mixed_worker

# ==========================================
# PASO 4: EJECUCIÓN CONCURRENTE
# ==========================================
echo -e "\n${GREEN}[3/5] Ejecutando ${TOTAL_REQUESTS} peticiones mixtas (${CONCURRENCY} hilos paralelos)...${NC}"

seq 1 "$TOTAL_REQUESTS" | xargs -n 1 -P "$CONCURRENCY" -I {} bash -c 'run_mixed_worker "$@"' _ {}

echo -e "\n${YELLOW}[4/5] Esperando ${WAIT_DLQ_SECONDS} segundos a que Spring AMQP agote los reintentos y mueva los eventos a la DLQ...${NC}"
sleep $WAIT_DLQ_SECONDS

# ==========================================
# PASO 5: CONSOLIDACIÓN DE RESULTADOS Y DLQ
# ==========================================
echo -e "\n${GREEN}[5/5] Analizando métricas de respuesta e inspeccionando RabbitMQ DLQ...${NC}\n"

VALID_EVENTS=$(grep -c "OK_VALID_EVENT" "$RESULTS_FILE" || true)
DLQ_STOCK_TRIGGERS=$(grep -c "TRIGGERED_DLQ_EXCESS_STOCK" "$RESULTS_FILE" || true)
DLQ_PRODUCT_TRIGGERS=$(grep -c "TRIGGERED_DLQ_INVALID_PRODUCT" "$RESULTS_FILE" || true)
FAILED_REQUESTS=$(grep -c "FAIL_ORDER_CREATION" "$RESULTS_FILE" || true)

# Consulta a la API de RabbitMQ para obtener el número real de mensajes retenidos en la DLQ
DLQ_INFO=$(curl -s -u "${RABBITMQ_USER}:${RABBITMQ_PASS}" "${RABBITMQ_API_URL}/queues/%2F/${DLQ_NAME}")
MESSAGES_IN_DLQ=$(echo "$DLQ_INFO" | jq -r '.messages // 0')

# Stock actual en catálogo
STOCK_FINAL=$(curl -s -X GET "${MS_CATALOG_URL}/api/v1/products/${PRODUCT_ID}" | jq -r '.data.stock')

EXPECTED_DLQ_TOTAL=$((DLQ_STOCK_TRIGGERS + DLQ_PRODUCT_TRIGGERS))

echo -e "${YELLOW}----------------------------------------------------${NC}"
echo -e "${YELLOW}           METRICAS DE PRUEBA Y DLQ                 ${NC}"
echo -e "${YELLOW}----------------------------------------------------${NC}"
echo -e " Eventos Exitosos Procesados        : ${GREEN}${VALID_EVENTS}${NC}"
echo -e " Eventos Forzados por Exceso Stock  : ${YELLOW}${DLQ_STOCK_TRIGGERS}${NC}"
echo -e " Eventos Forzados por Prod. Invalido: ${YELLOW}${DLQ_PRODUCT_TRIGGERS}${NC}"
echo -e " Errores HTTP Directos              : ${RED}${FAILED_REQUESTS}${NC}"
echo -e " Stock Final en Catalog             : ${CYAN}${STOCK_FINAL}${NC}"
echo -e " Mensajes Acumulados en DLQ Real    : ${CYAN}${MESSAGES_IN_DLQ}${NC} (Esperados >= ${EXPECTED_DLQ_TOTAL})"
echo -e "${YELLOW}----------------------------------------------------${NC}"

rm -f "$RESULTS_FILE"

if [ "$MESSAGES_IN_DLQ" -gt 0 ]; then
    echo -e "\n${GREEN}✔ PRUEBA DE CARGA Y DERIVACIÓN A DLQ COMPLETADA CON ÉXITO.${NC}"
else
    echo -e "\n${RED}✘ NO SE DETECTARON MENSAJES EN LA DLQ. REVISA LA CONFIGURACIÓN DE REINTENTOS O DLX.${NC}"
fi