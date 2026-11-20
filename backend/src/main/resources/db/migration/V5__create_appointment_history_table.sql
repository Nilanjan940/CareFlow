CREATE TABLE appointment_history (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    appointment_id BINARY(16) NOT NULL,

    action VARCHAR(30) NOT NULL,
    description VARCHAR(500) NOT NULL,

    old_values JSON,
    new_values JSON,

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_appointment_history PRIMARY KEY (id),

    CONSTRAINT fk_appointment_history_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id),

    CONSTRAINT fk_appointment_history_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id),

    INDEX idx_appointment_history_clinic_appointment
        (clinic_id, appointment_id, created_at)
);