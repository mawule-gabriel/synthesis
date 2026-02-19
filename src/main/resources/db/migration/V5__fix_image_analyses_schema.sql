-- Add missing column from BaseEntity
ALTER TABLE image_analyses ADD COLUMN active BOOLEAN DEFAULT TRUE NOT NULL;
