-- Arreglar columna status en tabla turns
-- Cambiar de tipo custom turn_status a VARCHAR

-- Primero, crear una columna temporal
ALTER TABLE turns ADD COLUMN status_temp VARCHAR(20);

-- Copiar datos con CAST
UPDATE turns SET status_temp = status::VARCHAR;

-- Eliminar la columna antigua
ALTER TABLE turns DROP COLUMN status;

-- Renombrar la columna temporal
ALTER TABLE turns RENAME COLUMN status_temp TO status;

-- Agregar restricción NOT NULL nuevamente
ALTER TABLE turns ALTER COLUMN status SET NOT NULL;

-- También arreglar turn_type por si acaso
ALTER TABLE turns ADD COLUMN turn_type_temp VARCHAR(20);
UPDATE turns SET turn_type_temp = turn_type::VARCHAR;
ALTER TABLE turns DROP COLUMN turn_type;
ALTER TABLE turns RENAME COLUMN turn_type_temp TO turn_type;
ALTER TABLE turns ALTER COLUMN turn_type SET NOT NULL;
