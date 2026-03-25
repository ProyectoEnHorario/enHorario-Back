# Guia de despliegue en Railway

## Archivo de esquema SQL

El archivo `railway_schema.sql` contiene:

1. Extensión pgcrypto para UUIDs
2. Tipos ENUM (user_role, establishment_status, turn_status, etc)
3. Función trigger para updated_at automático
4. Tablas principales con relaciones:
   - users
   - categories
   - establishments
   - establishment_hours
   - establishment_media
   - reviews
   - wait_time_reports
   - occupancy_timeslots
   - turns
   - favorites
   - notifications

## Paso a paso para Railway

### 1. Crear proyecto en Railway
- Ve a https://railway.app
- Click en "New Project"
- Elige "Infrastructure" → "PostgreSQL"

### 2. Obtener DATABASE_URL
- Abre la BD PostgreSQL en Railway
- En la pestaña "Connect", copia el connection string completo
- Debería verse como: `postgresql://user:password@host:port/dbname`

### 3. Ejecutar el esquema SQL
- En el panel de Railway, abre "Query Editor" para la BD PostgreSQL
- Copia y pega el contenido completo de `railway_schema.sql`
- Ejecuta (Ctrl + Enter o botón Run)

### 4. Desplegar backend
- En Railway, agrega un nuevo servicio: "New Service" → "Dockerfile"
- Selecciona el repo Git de este proyecto
- Railway detectará automáticamente el Dockerfile
- Configura variables de entorno:

```
DATABASE_URL=${{ Postgres.DATABASE_URL }}
DB_USERNAME=postgres
DB_PASSWORD=${{ Postgres.POSTGRES_PASSWORD }}
SERVER_PORT=8080
JWT_SECRET=min-256-bits-production-secret-key-here
```

### 5. Deploy automático
- Railway desplegará automáticamente cada vez que hagas push a main
- Puedes ver logs en tiempo real en Railway Dashboard

## Verificación

### Prueba desde terminal
```bash
curl https://your-railway-url/api/v1/health
```

Debería responder: `{"message": "EnHorario Backend is running!"}`

### Conexión desde Flutter
En tu app Flutter, configura el base URL:

```dart
final baseUrl = 'https://your-railway-backend-url/api/v1';
```

## Troubleshooting Railway

**Error: Connection refused**
- Verifica que PostgreSQL está running en Railway
- Verifica las credenciales DATABASE_URL

**Error: Port already in use**
- Railway asigna el puerto automáticamente, no necesitas configurarlo manualmente

**Error: Migrations failed**
- Ejecuta el SQL manualmente en Query Editor
- Verifica que el charset sea UTF-8

**Error: Docker build failed**
- Verifica que Java 17 está disponible
- Revisa los logs de build en Railway

## Scaling

Para escalar en Railway:
- Agrega réplicas del backend (Railway lo maneja automáticamente)
- PostgreSQL puede escalar con Railway Managed Database
- Configura Redis como caché opcional

## Base URL para la app

Después del despliegue, tu base URL será algo como:

```
https://enhorario-backend-prod.up.railway.app/api/v1
```

Úsala en la configuración de tu cliente Flutter.
