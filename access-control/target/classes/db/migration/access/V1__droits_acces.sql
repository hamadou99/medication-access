-- Faits lus par le module. Dans le système réel, ils viendraient de la gestion des droits de la communauté de référence.
-- Le rôle pmp_app doit exister avant la migration (créé par l'exploitation, avec son propre mot de passe).
CREATE SCHEMA access;

CREATE TABLE access.patient_settings (
    patient_id     text PRIMARY KEY,
    consent_active boolean NOT NULL DEFAULT true,
    emergency_mode text NOT NULL DEFAULT 'NORMAL_ONLY'
        CHECK (emergency_mode IN ('DISABLED', 'NORMAL_ONLY', 'NORMAL_AND_RESTRICTED'))
);

-- Droit d'accès accordé par le patient à un professionnel (art. 9 LDEP, art. 2 et 3 ODEP).
CREATE TABLE access.access_grant (
    person_id   text NOT NULL,
    patient_id  text NOT NULL REFERENCES access.patient_settings,
    level       text NOT NULL CHECK (level IN ('NORMAL', 'RESTREINT')),
    valid_from  timestamptz NOT NULL,
    valid_until timestamptz,
    PRIMARY KEY (person_id, patient_id)
);

-- Professionnel à qui le patient refuse tout accès (art. 4, let. b, ODEP).
CREATE TABLE access.access_exclusion (
    person_id  text NOT NULL,
    patient_id text NOT NULL REFERENCES access.patient_settings,
    PRIMARY KEY (person_id, patient_id)
);

CREATE TABLE access.representation_mandate (
    person_id   text NOT NULL,
    patient_id  text NOT NULL REFERENCES access.patient_settings,
    valid_from  timestamptz NOT NULL,
    valid_until timestamptz,
    revoked     boolean NOT NULL DEFAULT false,
    actions     text[] NOT NULL DEFAULT ARRAY['CONSULTER_PLAN', 'CONSULTER_JOURNAL'],
    PRIMARY KEY (person_id, patient_id)
);

GRANT USAGE ON SCHEMA access TO pmp_app;
GRANT SELECT ON ALL TABLES IN SCHEMA access TO pmp_app;
