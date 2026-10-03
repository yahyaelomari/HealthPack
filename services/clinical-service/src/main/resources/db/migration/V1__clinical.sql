-- One visit. patient_id / practitioner_id / appointment_id deliberately have
-- no foreign keys: those rows live in other services' databases.
CREATE TABLE encounter (
    id              uuid PRIMARY KEY,
    patient_id      uuid         NOT NULL,
    practitioner_id uuid         NOT NULL,
    appointment_id  uuid,
    type            varchar(16)  NOT NULL,
    status          varchar(16)  NOT NULL,
    started_at      timestamptz  NOT NULL,
    ended_at        timestamptz,
    reason          varchar(500),
    version         bigint       NOT NULL DEFAULT 0,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_encounter_period CHECK (ended_at IS NULL OR ended_at > started_at)
);

CREATE INDEX idx_encounter_patient_id ON encounter (patient_id);
CREATE INDEX idx_encounter_practitioner_id ON encounter (practitioner_id);

-- These two DO get a real foreign key: the encounter is in this database, and
-- an observation or diagnosis without its visit is meaningless, so cascade.
CREATE TABLE observation (
    id           uuid PRIMARY KEY,
    encounter_id uuid          NOT NULL REFERENCES encounter (id) ON DELETE CASCADE,
    code         varchar(32)   NOT NULL,
    display      varchar(200)  NOT NULL,
    value        numeric(12,3) NOT NULL,
    unit         varchar(32)   NOT NULL,
    recorded_at  timestamptz   NOT NULL,
    version      bigint        NOT NULL DEFAULT 0,
    created_at   timestamptz   NOT NULL DEFAULT now(),
    updated_at   timestamptz   NOT NULL DEFAULT now()
);

CREATE INDEX idx_observation_encounter_id ON observation (encounter_id);

-- clinical_condition, not condition: CONDITION is reserved in the SQL standard
CREATE TABLE clinical_condition (
    id              uuid PRIMARY KEY,
    encounter_id    uuid         NOT NULL REFERENCES encounter (id) ON DELETE CASCADE,
    code            varchar(32)  NOT NULL,
    display         varchar(200) NOT NULL,
    clinical_status varchar(16)  NOT NULL,
    onset_date      date,
    recorded_at     timestamptz  NOT NULL,
    version         bigint       NOT NULL DEFAULT 0,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_clinical_condition_encounter_id ON clinical_condition (encounter_id);
