CREATE TABLE queue_entries (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    doctor_id BINARY(16) NOT NULL,
    patient_id BINARY(16) NOT NULL,
    appointment_id BINARY(16),

    queue_date DATE NOT NULL,
    queue_number INT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',

    checked_in_at DATETIME(6) NOT NULL,
    started_at DATETIME(6),
    completed_at DATETIME(6),

    notes VARCHAR(1000),

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_queue_entries PRIMARY KEY (id),

    CONSTRAINT fk_queue_entries_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id),

    CONSTRAINT fk_queue_entries_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT fk_queue_entries_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id),

    CONSTRAINT fk_queue_entries_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id),

    CONSTRAINT uk_queue_doctor_date_number
        UNIQUE (doctor_id, queue_date, queue_number),

    INDEX idx_queue_clinic_date (clinic_id, queue_date),
    INDEX idx_queue_doctor_date_status (doctor_id, queue_date, status),
    INDEX idx_queue_patient (patient_id)
);