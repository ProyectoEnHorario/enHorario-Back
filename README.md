# EnHorario Backend - Spring Boot API

Backend REST API para la aplicación EnHorario (gestión de tiempos de espera y turnos).

## Estructura del Proyecto

```
enHorarioBack/
├── src/main/java/com/enhorario/
│   ├── config/               (Configuración: CORS, Seguridad, JWT)
│   ├── controller/           (Controladores REST: Auth, Establishments, Turns)
│   ├── model/                (Entidades JPA: User, Category, Establishment, Turn)
│   ├── repository/           (Repositorios Spring Data)
│   ├── service/              (Lógica de negocio: Auth, Establishments, Turns)
│   └── dto/                  (Data Transfer Objects)
├── src/test/java/            (Tests unitarios)
├── src/main/resources/
│   └── application.yml       (Configuración de Spring Boot)
├── pom.xml                   (Dependencias Maven)
├── Dockerfile                (Imagen Docker multi-stage)
├── docker-compose.yml        (Orquestación local: PostgreSQL + Backend)
└── README.md                 (Este archivo)
```

## Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- Docker y Docker Compose (opcional, para desarrollo local)
- PostgreSQL 15+ (si corres sin Docker)

## Configuración Local sin Docker

### 1. Crear la base de datos PostgreSQL

```bash
createdb enhorario
```

### 2. Ejecutar el esquema SQL

```bash
psql -U postgres -d enhorario -f ../railway_schema.sql
```

### 3. Configurar variables de entorno

Crea un archivo `.env` en la raíz del proyecto:

```
DATABASE_URL=jdbc:postgresql://localhost:5432/enhorario
DB_USERNAME=postgres
DB_PASSWORD=postgres
SERVER_PORT=8080
JWT_SECRET=your-super-secret-key-at-least-256-bits-long
```

### 4. Construir el proyecto

```bash
mvn clean install
```

### 5. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080/api/v1`.

## Desarrollo con Docker Compose

### 1. Construir e iniciar los contenedores

```bash
docker-compose up --build
```

La aplicación estará en `http://localhost:8080/api/v1`.
PostgreSQL estará en `localhost:5432`.

### 2. Ver logs

```bash
docker-compose logs -f backend
docker-compose logs -f postgres
```

### 3. Detener servicios

```bash
docker-compose down
```

## Despliegue en Railway

### 1. Crea un proyecto en Railway

### 2. Agrega un servicio PostgreSQL

- Railway creará automáticamente la BD y te dará un `DATABASE_URL`.

### 3. Ejecuta las migraciones SQL

Usa el Query Editor de Railway para ejecutar `railway_schema.sql`.

### 4. Crea un servicio Docker

- Apunta al repositorio Git de este proyecto (o sube el Dockerfile).
- Railway detectará el `Dockerfile` automáticamente.
- Asigna variables de entorno:

```
DATABASE_URL=${{ Postgres.DATABASE_URL }}
DB_USERNAME=postgres
DB_PASSWORD=${{ Postgres.POSTGRES_PASSWORD }}
SERVER_PORT=8080
JWT_SECRET=your-production-secret-key-min-256-bits
```

### 5. Despliega

Railway construirá y ejecutará automáticamente el contenedor.

## Endpoints Principales

### Autenticación

**POST** `/api/v1/auth/login`
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**POST** `/api/v1/auth/register`
```json
{
  "name": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "1234567890",
  "password": "password123"
}
```

**GET** `/api/v1/auth/me`
Headers: `Authorization: Bearer <token>`

### Establecimientos

**GET** `/api/v1/establishments?page=0&size=10`

**GET** `/api/v1/establishments/{id}`

**GET** `/api/v1/establishments/category/{categoryId}`

**GET** `/api/v1/establishments/search?query=banco&page=0&size=10`

**GET** `/api/v1/establishments/trending/shortest-wait`

### Turnos

**POST** `/api/v1/turns`
Headers: `Authorization: Bearer <token>`
```json
{
  "establishmentId": "550e8400-e29b-41d4-a716-446655440000",
  "turnType": "REGULAR"
}
```

**GET** `/api/v1/turns/my-turns`
Headers: `Authorization: Bearer <token>`

**GET** `/api/v1/turns/establishment/{establishmentId}`

**GET** `/api/v1/turns/{turnId}`

**PUT** `/api/v1/turns/{turnId}/status?status=CALLED`

**DELETE** `/api/v1/turns/{turnId}/cancel`
Headers: `Authorization: Bearer <token>`

### Health Check

**GET** `/api/v1/health`

## Tests

### Ejecutar todos los tests

```bash
mvn test
```

### Ejecutar tests específicos

```bash
mvn test -Dtest=AuthServiceTest
```

### Coverage

```bash
mvn test jacoco:report
```

## Flujo de Uso

1. **Usuario se registra** → POST `/auth/register` → Recibe UserDTO
2. **Usuario inicia sesión** → POST `/auth/login` → Recibe token JWT
3. **Usuario explora establecimientos** → GET `/establishments` con token
4. **Usuario solicita turno** → POST `/turns` con token
5. **Usuario ve sus turnos** → GET `/turns/my-turns` con token
6. **Admin actualiza estado de turno** → PUT `/turns/{id}/status` con token
7. **Usuario cancela turno** → DELETE `/turns/{id}/cancel` con token

## Convenciones de Código

- **Modelos**: siguen el patrón JPA con `@Entity`, timestamps automáticos (`createdAt`, `updatedAt`)
- **DTOs**: mapean requests/responses, nunca exponen entidades completas
- **Servicios**: contienen toda la lógica de negocio
- **Controladores**: validan, delegan a servicios, devuelven DTOs
- **Repositorios**: queries especializadas con `@Query` cuando es necesario

## Variables de Entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| DATABASE_URL | jdbc:postgresql://localhost:5432/enhorario | Conexión PostgreSQL |
| DB_USERNAME | postgres | Usuario BD |
| DB_PASSWORD | postgres | Contraseña BD |
| SERVER_PORT | 8080 | Puerto de la API |
| JWT_SECRET | your-secret-key... | Clave para firmar JWT (mínimo 256 bits en producción) |

## Troubleshooting

### Error: Conexión rechazada a PostgreSQL

- Verifica que PostgreSQL está corriendo: `psql -U postgres -c "SELECT 1;"`
- Verifica DATABASE_URL en `application.yml`

### Error: Puerto 8080 ya en uso

Cambia el puerto en `application.yml`:
```yaml
server:
  port: 8081
```

### Error en tests: BD no existe

Los tests usan H2 en memoria (ver `application-test.yml`). Maven debería inyectarla automáticamente en fase test.

## Contribuciones

1. Crea una rama feature: `git checkout -b feature/nombre`
2. Commit y push
3. Abre un PR contra `develop`

## Licencia

MIT
