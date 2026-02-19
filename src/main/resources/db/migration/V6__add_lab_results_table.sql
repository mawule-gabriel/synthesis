-- Create lab_results table to store structured laboratory data
CREATE TABLE lab_results (
    id BIGSERIAL PRIMARY KEY,
    consultation_id BIGINT NOT NULL,
    test_name VARCHAR(255) NOT NULL,
    numeric_value DECIMAL(19, 4),
    unit VARCHAR(50),
    is_abnormal BOOLEAN DEFAULT false,
    reference_range VARCHAR(255),
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT true,
    CONSTRAINT fk_lab_result_consultation FOREIGN KEY (consultation_id) REFERENCES consultations(id)
);

CREATE INDEX idx_lab_results_consultation_id ON lab_results(consultation_id);
