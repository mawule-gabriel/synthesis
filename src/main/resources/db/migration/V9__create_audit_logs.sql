-- Create audit_logs table for HIPAA-compliant patient data auditing
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT,
    provider_name VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255) NOT NULL,
    provider_role VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    patient_id BIGINT,
    patient_name VARCHAR(255),
    patient_national_id VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    session_id VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    CONSTRAINT fk_audit_provider FOREIGN KEY (provider_id) REFERENCES providers(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_provider ON audit_logs(provider_id);
CREATE INDEX idx_audit_patient ON audit_logs(patient_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_provider_patient ON audit_logs(provider_id, patient_id);
CREATE INDEX idx_audit_success ON audit_logs(success);

-- Add comment for documentation
COMMENT ON TABLE audit_logs IS 'HIPAA-compliant audit trail for all patient data access and modifications';
COMMENT ON COLUMN audit_logs.provider_id IS 'ID of the provider who performed the action (nullable if provider deleted)';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed (VIEW_PATIENT_PROFILE, UPDATE_PATIENT, etc.)';
COMMENT ON COLUMN audit_logs.patient_id IS 'ID of the patient whose data was accessed (nullable if patient deleted)';
COMMENT ON COLUMN audit_logs.details IS 'Additional context about the action (JSON format recommended)';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address of the client making the request';
COMMENT ON COLUMN audit_logs.timestamp IS 'Exact timestamp when the action occurred (immutable)';
COMMENT ON COLUMN audit_logs.success IS 'Whether the action completed successfully';
