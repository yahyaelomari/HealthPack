-- btree_gist is what lets the exclusion constraint below combine an equality
-- check (practitioner_id) with a range-overlap check (the time slot) in one
-- index — the standard btree operator class alone can't do the range half.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE practitioner (
    id          uuid PRIMARY KEY,
    npi         varchar(20)  NOT NULL,
    full_name   varchar(200) NOT NULL,
    specialty   varchar(50)  NOT NULL,
    version     bigint       NOT NULL DEFAULT 0,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT uk_practitioner_npi UNIQUE (npi)
);

CREATE TABLE appointment (
    id              uuid PRIMARY KEY,
    practitioner_id uuid         NOT NULL REFERENCES practitioner (id),
    -- No REFERENCES here: patient_id points at a row in patient-service's own
    -- database. A real foreign key across service boundaries isn't possible —
    -- existence is checked once, over gRPC, at booking time instead.
    patient_id      uuid         NOT NULL,
    slot_start      timestamptz  NOT NULL,
    slot_end        timestamptz  NOT NULL,
    status          varchar(16)  NOT NULL,
    reason          varchar(500),
    version         bigint       NOT NULL DEFAULT 0,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT chk_appointment_slot_order CHECK (slot_end > slot_start),

    -- The actual guarantee: two appointments for the same practitioner can
    -- never overlap in time, enforced by the database itself rather than an
    -- application-level check that would race under concurrency. CANCELLED
    -- is excluded deliberately — a cancelled appointment must free its slot,
    -- not keep blocking it forever.
    CONSTRAINT no_double_booking EXCLUDE USING gist (
        practitioner_id WITH =,
        tstzrange(slot_start, slot_end) WITH &&
    ) WHERE (status <> 'CANCELLED')
);

CREATE INDEX idx_appointment_practitioner_id ON appointment (practitioner_id);
CREATE INDEX idx_appointment_patient_id ON appointment (patient_id);
