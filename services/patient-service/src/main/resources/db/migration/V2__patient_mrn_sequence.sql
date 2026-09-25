-- Backs MrnGenerator. A sequence guarantees uniqueness under concurrency for
-- free — two callers can never receive the same value — which is exactly the
-- property a hand-rolled "read max, add one" scheme in Java cannot give you.
CREATE SEQUENCE patient_mrn_seq START WITH 1 INCREMENT BY 1;
