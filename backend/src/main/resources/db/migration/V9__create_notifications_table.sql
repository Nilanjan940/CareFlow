CREATE TABLE notifications (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    appointment_id BINARY(16) NOT NULL,
    patient_id BINARY(16) NOT NULL,

    notification_type VARCHAR(40) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    scheduled_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6),
    error_message VARCHAR(1000),

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_notifications PRIMARY KEY (id),

    CONSTRAINT fk_notifications_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id),

    CONSTRAINT fk_notifications_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id),

    CONSTRAINT fk_notifications_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id),

    CONSTRAINT uk_notifications_appointment_type_channel
        UNIQUE (appointment_id, notification_type, channel),

    CONSTRAINT chk_notifications_status
        CHECK (status IN ('PENDING', 'SENT', 'FAILED')),

    CONSTRAINT chk_notifications_channel
        CHECK (channel IN ('EMAIL', 'SMS')),

    INDEX idx_notifications_due
        (status, scheduled_at),

    INDEX idx_notifications_clinic
        (clinic_id, status, scheduled_at)
);