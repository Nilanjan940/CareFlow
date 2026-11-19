CREATE TABLE doctors (
    id BINARY(16) NOT NULL,
    clinic_id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    specialization VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_doctors PRIMARY KEY (id),
    CONSTRAINT uk_doctors_clinic_email UNIQUE (clinic_id, email),
    CONSTRAINT fk_doctors_clinic
        FOREIGN KEY (clinic_id) REFERENCES clinics(id)
);