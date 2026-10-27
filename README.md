# Pedidos360 - Microservicio de Catálogo

## Descripción

Microservicio de catálogo para el sistema Pedidos360. Su responsabilidad es gestionar el inventario de productos, categorías, precios y movimientos de stock del negocio.

Este servicio está desarrollado en Java con Spring Boot y usa MongoDB como base de datos NoSQL. Expone una API REST para permitir la gestión del catálogo, integrándose con el BFF y con otros servicios del ecosistema de Pedidos360.

## Estado del proyecto

El proyecto se encuentra en desarrollo activo. La base funcional ya está implementada y la arquitectura está pensada para crecer hacia integración con colas SQS y otros servicios del sistema.

## Objetivos funcionales

- Gestionar productos del catálogo
- Gestionar categorías y clasificación de productos
- Registrar cambios de precio con historial
- Controlar stock y movimientos de inventario
- Mantener trazabilidad por usuario y correlation ID
- Exponer una API REST documentada con Swagger/OpenAPI
- Integrarse con autenticación JWT mediante Keycloak

## Stack tecnológico

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data MongoDB
- Spring Security + OAuth2 Resource Server
- Spring Validation
- OpenAPI / Swagger UI
- AWS SQS (planeado/integración en evolución)
- Lombok
- Maven
- MongoDB
- Actuator

## Arquitectura

El microservicio sigue una arquitectura por capas:

- `controller`: endpoints REST
- `service`: lógica de negocio
- `repository`: acceso a datos con MongoDB
- `entity`: modelos de persistencia
- `dto`: request/response DTOs
- `mapper`: conversión entre entidades y DTOs
- `exception`: manejo centralizado de errores
- `config`: configuración global
- `security`: autenticación y autorización
- `observability`: trazabilidad y correlation ID

## Estructura principal

```text
src/
├── main/
│   ├── java/cl/pedidos360/ms_catalog/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── observability/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   ├── audit/
│   │   └── MsCatalogApplication.java
│   └── resources/
│       ├── application.yaml
│       ├── application-dev.yaml
│       └── application-prod.yaml
├── test/
│   └── java/cl/pedidos360/ms_catalog/
└── pom.xml
```

## Dominio principal

### Productos

El producto contiene información esencial como:

- SKU único
- nombre
- descripción
- precio
- stock actual
- categoría asociada
- estado del producto
- auditoría de creación y actualización

### Categorías

Las categorías permiten agrupar productos y mantener un catálogo estructurado.

### Historial de precios

Cada cambio de precio queda registrado, permitiendo auditar el histórico de precio por producto.

### Movimientos de stock

Cada ajuste, reposición o carga inicial de stock genera un movimiento con información útil para trazabilidad e inventario.

## API REST

La aplicación expone los endpoints bajo el prefijo principal:

- `/api/v1/products`
- `/api/v1/categories`

### Endpoints de productos

- `POST /api/v1/products` - Crear producto
- `GET /api/v1/products/{productId}` - Obtener producto por ID
- `GET /api/v1/products/search` - Buscar productos con filtros
- `PUT /api/v1/products/{productId}` - Actualizar producto
- `PATCH /api/v1/products/{productId}/status` - Actualizar estado
- `PATCH /api/v1/products/{productId}/price` - Actualizar precio
- `PATCH /api/v1/products/{productId}/stock` - Ajustar stock
- `PATCH /api/v1/products/{productId}/restock` - Reponer stock
- `DELETE /api/v1/products/{productId}` - Eliminar lógicamente

### Endpoints de categorías

- `GET /api/v1/categories`
- `POST /api/v1/categories`
- `PUT /api/v1/categories/{categoryId}`
- `DELETE /api/v1/categories/{categoryId}`

## Seguridad

La API utiliza autenticación JWT con Spring Security y OAuth2 Resource Server.

### Política actual

- Lecturas del catálogo: públicas
- Operaciones de escritura: protegidas por roles
- Roles esperados para administración y operación: `ADMINISTRADOR`, `OPERADOR`

Esto permite que los clientes puedan consultar productos sin autenticación, mientras las acciones sensibles requieren permisos.

## Swagger / OpenAPI

La API está documentada con SpringDoc OpenAPI.

URLs de documentacion:

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`

## Observabilidad

El proyecto incorpora trazabilidad con:

- `CorrelationId` para identificar cada solicitud
- logs con usuario y operación
- soporte para monitoreo mediante Actuator

## Configuración

### Variables principales

```properties
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8081
MONGODB_URI=mongodb://localhost:27017/pedidos360_catalog?replicaSet=rs0
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
AWS_SQS_ENDPOINT=http://localhost:4567
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/pedidos360
JWT_ROLES_CLAIM=realm_access.roles
JWT_ROLES_PREFIX=ROLE_
```

## Requerimientos locales

- Java 21+
- Maven
- MongoDB
- Docker (opcional para levantar dependencias locales)
- Keycloak (para JWT)
- LocalStack o entorno AWS compatible para SQS

## Ejecución local

```bash
./mvnw clean install
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## Verificación

```bash
curl http://localhost:8081/actuator/health
```

## Testing

Se cuenta con pruebas para:

- controladores
- servicios
- mappers
- exception handlers
- configuración
- seguridad
- repositorios

Ejecutar tests:

```bash
./mvnw test
```

## Proximos pasos

El proyecto ya tiene una base funcional y una arquitectura adecuada para crecer. Los próximos pasos importantes son:

1. Completar integración con colas SQS
2. Definir y documentar contratos de eventos entre servicios
3. Finalizar configuraciones de producción
4. Mejorar documentación operativa y README técnico
5. Fortalecer pruebas de integración y escenarios edge

## Consideraciones de desarrollo

El proyecto está en desarrollo y aún no completa toda la integración con eventos asíncronos ni la documentación final del ecosistema. Sin embargo, la estructura actual demuestra una base sólida para seguir avanzando hacia una implementación más completa y preparada para producción.

## Licencia

Proyecto académico / personal. Verificar licencia según política del repositorio.

## Autor

Benja Lizama

## Notas

Este README está orientado a ofrecer una visión general clara del microservicio, su arquitectura y sus necesidades de evolución, especialmente en el contexto del sistema Pedidos360.
