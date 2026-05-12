-- ============================================================================
-- SQL MIGRATION para actualizar roles de usuario en EnHorario Backend
-- ============================================================================
-- Este script actualiza el enum user_role para soportar los nuevos roles:
-- - SUPERADMIN: Administrador del sistema (crear ADMIN, modificar todos)
-- - ADMIN: Administrador regional/de zona (crear ADMIN_LOCAL)
-- - ADMIN_LOCAL: Administrador de establecimiento local
-- - USER: Usuario regular (ya existía)
-- ============================================================================

-- Paso 1: Verificar si el tipo enum user_role existe
-- Si el tipo enum no existe, crearlo con todos los valores
DO $$
BEGIN
  -- Intentar crear el tipo enum si no existe
  IF NOT EXISTS (
    SELECT 1 FROM pg_type 
    WHERE typname = 'user_role' AND typcategory = 'E'
  ) THEN
    CREATE TYPE user_role AS ENUM ('SUPERADMIN', 'ADMIN', 'ADMIN_LOCAL', 'USER');
    RAISE NOTICE 'Tipo enum user_role creado con todos los valores';
  ELSE
    -- El tipo existe, verificar si tiene todos los valores necesarios
    IF NOT EXISTS (
      SELECT 1 FROM pg_enum 
      WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'user_role')
      AND enumlabel = 'SUPERADMIN'
    ) THEN
      -- Agregar SUPERADMIN al principio
      ALTER TYPE user_role ADD VALUE 'SUPERADMIN' BEFORE 'ADMIN';
      RAISE NOTICE 'Valor SUPERADMIN agregado al enum user_role';
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM pg_enum 
      WHERE enumtypid = (SELECT oid FROM pg_type WHERE typname = 'user_role')
      AND enumlabel = 'ADMIN_LOCAL'
    ) THEN
      -- Agregar ADMIN_LOCAL después de ADMIN
      ALTER TYPE user_role ADD VALUE 'ADMIN_LOCAL' AFTER 'ADMIN';
      RAISE NOTICE 'Valor ADMIN_LOCAL agregado al enum user_role';
    END IF;
  END IF;
END $$;

-- Paso 2: Verificar que la tabla users existe y tiene la columna role
-- (normalmente Hibernate/JPA crea esto automáticamente, pero aquí verificamos)
ALTER TABLE IF EXISTS users
  ALTER COLUMN role TYPE user_role USING role::user_role;

-- Paso 3: Seed de usuario SUPERADMIN para pruebas (opcional)
-- Descomenta la siguiente línea si quieres crear un usuario SUPERADMIN de prueba
-- INSERT INTO users (
--   id, name, last_name, email, role, phone, profile_photo_url,
--   password_hash, is_active, created_at, updated_at, deleted_at
-- ) VALUES (
--   'cccccccc-cccc-cccc-cccc-cccccccccccc',
--   'Superadmin',
--   'Dev',
--   'superadmin.dev@enhorario.com',
--   'SUPERADMIN'::user_role,
--   '3000000002',
--   NULL,
--   '$2a$10$B6VrYy3PZCn3WpMqk9YXZOpazz1trsHsWoCcUISxfGFAqFozauPKG',
--   true,
--   NOW(),
--   NOW(),
--   NULL
-- ) ON CONFLICT (email) DO NOTHING;

-- Paso 4: Actualizar usuarios existentes si es necesario (ejemplo: cambiar algunos ADMIN a SUPERADMIN)
-- Descomenta y ajusta según sea necesario:
-- UPDATE users SET role = 'SUPERADMIN'::user_role 
-- WHERE email = 'admin.dev@enhorario.com';

COMMIT;
