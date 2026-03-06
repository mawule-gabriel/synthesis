-- Widen treatments columns to accommodate AI-generated text
ALTER TABLE treatments ALTER COLUMN type TYPE VARCHAR(500);
ALTER TABLE treatments ALTER COLUMN dosage TYPE TEXT;
ALTER TABLE treatments ALTER COLUMN duration TYPE VARCHAR(500);
