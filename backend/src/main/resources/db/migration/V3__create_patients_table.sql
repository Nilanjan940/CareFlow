CREATE TABLE patients (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_patients PRIMARY KEY (id),
    CONSTRAINT fk_patients_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id)
);