BEGIN;

-- Categorias base de pruebas
INSERT INTO categories (id, name, icon_name, is_active, created_at, updated_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'Bancos Dev', 'account_balance', true, NOW(), NOW()),
  ('33333333-3333-3333-3333-333333333333', 'Restaurantes Dev', 'restaurant', true, NOW(), NOW()),
  ('44444444-4444-4444-4444-444444444444', 'Comercios Dev', 'store', true, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  icon_name = EXCLUDED.icon_name,
  is_active = EXCLUDED.is_active,
  updated_at = NOW();

-- Usuarios de pruebas (role puede ser varchar o enum en Railway)
DO $$
DECLARE
  role_type text;
BEGIN
  SELECT format_type(a.atttypid, a.atttypmod)
  INTO role_type
  FROM pg_attribute a
  JOIN pg_class c ON c.oid = a.attrelid
  JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public'
    AND c.relname = 'users'
    AND a.attname = 'role'
    AND a.attnum > 0
    AND NOT a.attisdropped;

  EXECUTE format(
    $fmt$
    INSERT INTO users (
      id, name, last_name, email, role, phone, profile_photo_url,
      password_hash, is_active, created_at, updated_at, deleted_at
    ) VALUES
      (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Admin',
        'Dev',
        'admin.dev@enhorario.com',
        'ADMIN'::%s,
        '3000000000',
        NULL,
        '$2a$10$B6VrYy3PZCn3WpMqk9YXZOpazz1trsHsWoCcUISxfGFAqFozauPKG',
        true,
        NOW(),
        NOW(),
        NULL
      ),
      (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Usuario',
        'Dev',
        'usuario.dev@enhorario.com',
        'USER'::%s,
        '3000000001',
        NULL,
        '$2a$10$B6VrYy3PZCn3WpMqk9YXZOpazz1trsHsWoCcUISxfGFAqFozauPKG',
        true,
        NOW(),
        NOW(),
        NULL
      )
    ON CONFLICT (email) DO UPDATE SET
      name = EXCLUDED.name,
      last_name = EXCLUDED.last_name,
      role = EXCLUDED.role,
      phone = EXCLUDED.phone,
      password_hash = EXCLUDED.password_hash,
      is_active = EXCLUDED.is_active,
      updated_at = NOW(),
      deleted_at = NULL
    $fmt$,
    role_type,
    role_type
  );
END $$;

-- Establecimientos de prueba enlazados a categorias y creador
INSERT INTO establishments (
  id,
  name,
  rating_avg,
  rating_count,
  category_id,
  short_description,
  long_description,
  address_line,
  city,
  state_region,
  country,
  latitude,
  longitude,
  status,
  opening_time,
  closing_time,
  website_url,
  phone,
  whatsapp_url,
  cover_photo_url,
  logo_url,
  average_wait_minutes,
  peak_hour_start,
  low_hour_start,
  price_level,
  is_verified,
  is_active,
  created_by_user_id,
  created_at,
  updated_at
)
VALUES
  (
    '22222222-2222-2222-2222-222222222222',
    'Banco Centro Dev',
    4.2,
    18,
    '11111111-1111-1111-1111-111111111111',
    'Sucursal de pruebas backend',
    'Sucursal para pruebas integrales de turnos y listados.',
    'Calle 10 # 20-30',
    'Bogota',
    'Cundinamarca',
    'Colombia',
    4.7110,
    -74.0721,
    'OPEN',
    NULL,
    NULL,
    'https://example.com/banco-centro-dev',
    '3001234567',
    'https://wa.me/573001234567',
    NULL,
    NULL,
    12,
    NULL,
    NULL,
    2,
    true,
    true,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    NOW(),
    NOW()
  ),
  (
    '55555555-5555-5555-5555-555555555555',
    'Restaurante Norte Dev',
    4.7,
    25,
    '33333333-3333-3333-3333-333333333333',
    'Restaurante de validacion',
    'Restaurante para probar flujo de categorias y tendencias.',
    'Carrera 15 # 100-25',
    'Bogota',
    'Cundinamarca',
    'Colombia',
    4.6791,
    -74.0540,
    'OPEN',
    NULL,
    NULL,
    'https://example.com/restaurante-norte-dev',
    '3007654321',
    'https://wa.me/573007654321',
    NULL,
    NULL,
    20,
    NULL,
    NULL,
    3,
    true,
    true,
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    NOW(),
    NOW()
  )
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name,
  rating_avg = EXCLUDED.rating_avg,
  rating_count = EXCLUDED.rating_count,
  category_id = EXCLUDED.category_id,
  short_description = EXCLUDED.short_description,
  long_description = EXCLUDED.long_description,
  address_line = EXCLUDED.address_line,
  city = EXCLUDED.city,
  state_region = EXCLUDED.state_region,
  country = EXCLUDED.country,
  latitude = EXCLUDED.latitude,
  longitude = EXCLUDED.longitude,
  status = EXCLUDED.status,
  opening_time = EXCLUDED.opening_time,
  closing_time = EXCLUDED.closing_time,
  website_url = EXCLUDED.website_url,
  phone = EXCLUDED.phone,
  whatsapp_url = EXCLUDED.whatsapp_url,
  average_wait_minutes = EXCLUDED.average_wait_minutes,
  peak_hour_start = EXCLUDED.peak_hour_start,
  low_hour_start = EXCLUDED.low_hour_start,
  price_level = EXCLUDED.price_level,
  is_verified = EXCLUDED.is_verified,
  is_active = EXCLUDED.is_active,
  created_by_user_id = EXCLUDED.created_by_user_id,
  updated_at = NOW();

-- Turnos de prueba en estados distintos
INSERT INTO turns (
  id,
  user_id,
  establishment_id,
  turn_code,
  turn_type,
  status,
  queue_position,
  requested_at,
  called_at,
  attended_at,
  cancelled_at,
  estimated_attention_at
)
VALUES
  (
    '66666666-6666-6666-6666-666666666666',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '22222222-2222-2222-2222-222222222222',
    'A-001',
    'REGULAR',
    'WAITING',
    1,
    NOW() - INTERVAL '10 minutes',
    NULL,
    NULL,
    NULL,
    NOW() + INTERVAL '8 minutes'
  ),
  (
    '77777777-7777-7777-7777-777777777777',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '22222222-2222-2222-2222-222222222222',
    'A-002',
    'REGULAR',
    'CALLED',
    2,
    NOW() - INTERVAL '22 minutes',
    NOW() - INTERVAL '2 minutes',
    NULL,
    NULL,
    NOW() - INTERVAL '1 minutes'
  ),
  (
    '88888888-8888-8888-8888-888888888888',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '55555555-5555-5555-5555-555555555555',
    'P-001',
    'PRIORITY',
    'ATTENDED',
    1,
    NOW() - INTERVAL '40 minutes',
    NOW() - INTERVAL '25 minutes',
    NOW() - INTERVAL '5 minutes',
    NULL,
    NOW() - INTERVAL '20 minutes'
  )
ON CONFLICT (id) DO UPDATE SET
  user_id = EXCLUDED.user_id,
  establishment_id = EXCLUDED.establishment_id,
  turn_code = EXCLUDED.turn_code,
  turn_type = EXCLUDED.turn_type,
  status = EXCLUDED.status,
  queue_position = EXCLUDED.queue_position,
  requested_at = EXCLUDED.requested_at,
  called_at = EXCLUDED.called_at,
  attended_at = EXCLUDED.attended_at,
  cancelled_at = EXCLUDED.cancelled_at,
  estimated_attention_at = EXCLUDED.estimated_attention_at;

COMMIT;

-- Resumen final de seed
SELECT 'categories' AS entity, COUNT(*) AS total FROM categories
UNION ALL
SELECT 'users' AS entity, COUNT(*) AS total FROM users
UNION ALL
SELECT 'establishments' AS entity, COUNT(*) AS total FROM establishments
UNION ALL
SELECT 'turns' AS entity, COUNT(*) AS total FROM turns;
