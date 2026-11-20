CREATE TABLE doctor_availability_sessions (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    doctor_id BINARY(16) NOT NULL,

    day_of_week VARCHAR(9) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_doctor_availability_sessions
        PRIMARY KEY (id),

    CONSTRAINT fk_availability_sessions_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id),

    CONSTRAINT fk_availability_sessions_doctor
        FOREIGN KEY (doctor_id) REFERENCES doctors(id),

    CONSTRAINT chk_availability_session_time
        CHECK (end_time > start_time),

    INDEX idx_availability_clinic_doctor_day
        (clinic_id, doctor_id, day_of_week)
);