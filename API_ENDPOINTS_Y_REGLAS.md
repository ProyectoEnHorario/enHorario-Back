# API EnHorario - Endpoints y Reglas

Base URL actual: /api/v1

## 1) Salud del servicio

### GET /health
- Descripcion: valida que el backend este activo.
- Auth: no requiere.
- Respuesta 200: texto plano "EnHorario Backend is running!"

## 2) Autenticacion

### POST /auth/register
- Descripcion: registra un usuario nuevo.
- Auth: no requiere.
- Body esperado:
  - name: string obligatorio por modelo (columna users.name NOT NULL).
  - lastName: string obligatorio por modelo (columna users.last_name NOT NULL).
  - email: string obligatorio y unico.
  - phone: string opcional.
  - password: string obligatorio.
- Regla de rol:
  - El rol en persistencia usa enum nativo PostgreSQL user_role.
  - El backend asigna rol USER al registrar desde este endpoint.
- Exitoso: 200 con UserDTO.
- Errores comunes: 400 en payload invalido, email duplicado u otras excepciones de negocio.

### POST /auth/login
- Descripcion: autentica usuario por email/password.
- Auth: no requiere.
- Body esperado:
  - email: string.
  - password: string.
- Exitoso: 200 con LoginResponseDTO:
  - token: string JWT.
  - user: UserDTO.
- Error: 400 cuando credenciales o request no validan.

### GET /auth/me
- Descripcion: retorna usuario actual segun header Authorization.
- Auth: requiere header Authorization: Bearer <valor>.
- Nota importante:
  - Actualmente el controlador extrae literalmente el valor despues de Bearer y lo trata como email.
  - Esto no parsea JWT aqui; depende de la logica de servicio actual.
- Exitoso: 200 con UserDTO.
- Error: 400 si header no llega en formato esperado o falla la busqueda.

## 3) Establecimientos

### GET /establishments
- Descripcion: lista establecimientos paginados.
- Auth: no requiere.
- Query params:
  - page (opcional, default 0)
  - size (opcional, default 10)
- Exitoso: 200 con Page<EstablishmentDTO>.

### GET /establishments/{id}
- Descripcion: obtiene un establecimiento por UUID.
- Auth: no requiere.
- Exitoso: 200 con EstablishmentDTO.
- Error: 404 si no existe o id invalido.

### GET /establishments/category/{categoryId}
- Descripcion: lista establecimientos por categoria.
- Auth: no requiere.
- Exitoso: 200 con lista de EstablishmentDTO.
- Error: 400 si categoryId invalido o error de negocio.

### GET /establishments/search
- Descripcion: busca establecimientos por texto.
- Auth: no requiere.
- Query params:
  - query (obligatorio)
  - page (opcional, default 0)
  - size (opcional, default 10)
- Exitoso: 200 con Page<EstablishmentDTO>.

### GET /establishments/trending/shortest-wait
- Descripcion: lista establecimientos con menor tiempo de espera.
- Auth: no requiere.
- Exitoso: 200 con lista de EstablishmentDTO.

## 4) Turnos

### POST /turns
- Descripcion: crea un turno para el usuario autenticado.
- Auth: requiere Authorization: Bearer <valor>.
- Body esperado (CreateTurnRequestDTO):
  - establishmentId: string UUID.
  - turnType: string enum (REGULAR o PRIORITY).
- Exitoso: 200 con TurnDTO.
- Error: 400 por header invalido, datos invalidos o reglas de negocio.

### GET /turns/my-turns
- Descripcion: lista turnos del usuario autenticado.
- Auth: requiere Authorization: Bearer <valor>.
- Exitoso: 200 con lista de TurnDTO.
- Error: 400 si token/header invalido.

### GET /turns/establishment/{establishmentId}
- Descripcion: lista turnos de un establecimiento.
- Auth: no requiere.
- Exitoso: 200 con lista de TurnDTO.
- Error: 400 si id invalido o error interno.

### GET /turns/{turnId}
- Descripcion: detalle de turno por id.
- Auth: no requiere.
- Exitoso: 200 con TurnDTO.
- Error: 404 si no existe.

### PUT /turns/{turnId}/status?status=VALOR
- Descripcion: actualiza estado de turno.
- Auth: no requiere en SecurityConfig actual.
- Param requerido:
  - status: WAITING, CALLED, ATTENDED, CANCELLED o EXPIRED.
- Exitoso: 200 con TurnDTO actualizado.
- Error: 400 en estado invalido o transicion no permitida por negocio.

### DELETE /turns/{turnId}/cancel
- Descripcion: cancela un turno del usuario autenticado.
- Auth: requiere Authorization: Bearer <valor>.
- Exitoso: 200 sin body.
- Error: 400 si token invalido o reglas de negocio no permiten cancelar.

## 5) Reglas tecnicas transversales

## Seguridad HTTP (SecurityConfig)
- Actualmente todos los endpoints quedan permitidos (anyRequest().permitAll()).
- /api/v1/health y /api/v1/auth/** estan explicitamente permitidos.
- CSRF deshabilitado.
- Sesion stateless.

## Reglas de datos importantes de modelo
- users.email: unico.
- users.name y users.lastName: obligatorios (NOT NULL).
- users.role: enum PostgreSQL user_role (ADMIN/USER).
- turns.turnType: enum REGULAR/PRIORITY.
- turns.status: enum WAITING/CALLED/ATTENDED/CANCELLED/EXPIRED.

## Recomendaciones para cliente Flutter
- Siempre enviar name y lastName en registro.
- Manejar 400 con mensaje generico y errores por campo cuando aplique.
- En login guardar token y usar Authorization: Bearer <token>.
- Para update de estado de turno usar query param status, no body.
