CREATE TABLE appointments (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    doctor_id BINARY(16) NOT NULL,
    patient_id BINARY(16) NOT NULL,

    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000),

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_appointments PRIMARY KEY (id),

    CONSTRAINT fk_appointments_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id),

    CONSTRAINT fk_appointments_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT fk_appointments_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id),

    INDEX idx_appointments_clinic_date (clinic_id, appointment_date),
    INDEX idx_appointments_doctor_date (doctor_id, appointment_date)
);