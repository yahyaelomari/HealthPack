CREATE TABLE patient (
    id                   uuid PRIMARY KEY,
    mrn                  varchar(32)  NOT NULL,
    first_name           varchar(100) NOT NULL,
    last_name            varchar(100) NOT NULL,
    birth_date           date         NOT NULL,
    gender               varchar(16)  NOT NULL,
    status               varchar(16)  NOT NULL,
    deceased_date        date,
    email                varchar(255),
    phone                varchar(32),
    address_line1        varchar(200),
    address_line2        varchar(200),
    address_city         varchar(100),
    address_postal_code  varchar(20),
    address_country_code varchar(2),
    version              bigint       NOT NULL DEFAULT 0,
    created_at           timestamptz  NOT NULL DEFAULT now(),
    updated_at           timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT uk_patient_mrn UNIQUE (mrn)
);

CREATE INDEX idx_patient_last_name ON patient (last_name);
CREATE INDEX idx_patient_birth_date ON patient (birth_date);
