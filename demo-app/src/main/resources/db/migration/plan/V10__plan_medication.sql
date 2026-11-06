-- Plan de médication de démonstration : lecture rapide (treatment) et documents CH EMED conservés tels quels (document).
CREATE SCHEMA plan;

CREATE TABLE plan.patient (
    id         text PRIMARY KEY,
    family     text NOT NULL,
    given      text NOT NULL,
    gender     text NOT NULL,
    birth_date text NOT NULL
);

CREATE TABLE plan.treatment (
    id          uuid PRIMARY KEY,
    document_id uuid NOT NULL,
    patient_id  text NOT NULL REFERENCES plan.patient,
    atc         text NOT NULL,
    medication  text NOT NULL,
    dosage      text NOT NULL,
    level       text NOT NULL CHECK (level IN ('NORMAL', 'RESTREINT', 'SECRET')),
    status      text NOT NULL CHECK (status IN ('active', 'suspended', 'cancelled', 'refused')),
    created_by  text NOT NULL,
    created_at  timestamptz NOT NULL
);
CREATE INDEX treatment_patient ON plan.treatment (patient_id);

CREATE TABLE plan.document (
    id           uuid PRIMARY KEY,
    treatment_id uuid NOT NULL REFERENCES plan.treatment,
    patient_id   text NOT NULL REFERENCES plan.patient,
    type         text NOT NULL CHECK (type IN ('MTP', 'PRE', 'DIS', 'PADV')),
    level        text NOT NULL,
    author       text NOT NULL,
    created_at   timestamptz NOT NULL,
    fhir         jsonb NOT NULL
);
CREATE INDEX document_treatment ON plan.document (treatment_id);

GRANT USAGE ON SCHEMA plan TO pmp_app;
GRANT SELECT ON plan.patient TO pmp_app;
GRANT SELECT, INSERT, UPDATE ON plan.treatment TO pmp_app;
GRANT SELECT, INSERT ON plan.document TO pmp_app;
