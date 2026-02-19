-- Create image_analyses table to persist Interpreted Imaging findings
CREATE TABLE image_analyses (
    id BIGSERIAL PRIMARY KEY,
    consultation_id BIGINT REFERENCES consultations(id),
    description TEXT NOT NULL,
    findings JSONB,
    analyzed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_image_analyses_consultation_id ON image_analyses(consultation_id);
