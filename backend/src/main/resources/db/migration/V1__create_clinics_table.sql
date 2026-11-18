CREATE TABLE clinics (
    id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_clinics PRIMARY KEY (id),
    CONSTRAINT uk_clinics_email UNIQUE (email)
);
