-- Journal d'audit : ajout seul. Le trigger bloque aussi le propriétaire ; un super-utilisateur qui le contournerait
-- serait trahi par la chaîne de hachage, dont la clé est conservée hors de la base.
CREATE SCHEMA audit;

CREATE TABLE audit.audit_event (
    seq                     bigint PRIMARY KEY,
    event_uuid              uuid NOT NULL UNIQUE,
    occurred_at             timestamptz NOT NULL,
    person_id               text NOT NULL,
    person_name             text,
    acting_role             text,
    patient_id              text,
    resource                text NOT NULL,
    action                  text NOT NULL,
    atc_event               text,
    outcome                 text NOT NULL CHECK (outcome IN ('PERMIT', 'DENY', 'EMERGENCY_PERMIT')),
    reason_code             text NOT NULL,
    reason_text             text NOT NULL,
    purpose_of_use          text NOT NULL CHECK (purpose_of_use IN ('NORM', 'EMER')),
    emergency_justification text,
    reference               text,
    policy_version          text NOT NULL,
    prev_hash               char(64) NOT NULL,
    hash                    char(64) NOT NULL
);

CREATE INDEX audit_event_patient ON audit.audit_event (patient_id, seq DESC);
CREATE INDEX audit_event_person_time ON audit.audit_event (person_id, occurred_at);
CREATE INDEX audit_event_emergency ON audit.audit_event (seq) WHERE outcome = 'EMERGENCY_PERMIT';

-- Tête de chaîne : le verrou sur cette ligne sérialise les ajouts.
CREATE TABLE audit.chain_head (
    id        smallint PRIMARY KEY CHECK (id = 1),
    last_seq  bigint NOT NULL,
    last_hash char(64) NOT NULL
);
INSERT INTO audit.chain_head VALUES (1, 0, repeat('0', 64));

-- Contrôle a posteriori d'un accès d'urgence. Une nouvelle revue complète la précédente sans la remplacer.
CREATE TABLE audit.emergency_review (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_seq   bigint NOT NULL REFERENCES audit.audit_event,
    reviewer_id text NOT NULL,
    verdict     text NOT NULL CHECK (verdict IN ('JUSTIFIE', 'A_INSTRUIRE', 'ABUSIF')),
    comment     text,
    reviewed_at timestamptz NOT NULL
);

-- File d'information du patient après un accès d'urgence (art. 9, al. 5, LDEP ; art. 2, al. 2, ODEP).
CREATE TABLE audit.patient_notification (
    event_seq  bigint PRIMARY KEY REFERENCES audit.audit_event,
    patient_id text NOT NULL,
    created_at timestamptz NOT NULL,
    sent_at    timestamptz
);

CREATE FUNCTION audit.refuse_change() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Le journal d''audit est en ajout seul (% refusé sur %)', TG_OP, TG_TABLE_NAME
        USING ERRCODE = 'integrity_constraint_violation';
END $$;

CREATE TRIGGER audit_event_append_only BEFORE UPDATE OR DELETE ON audit.audit_event
    FOR EACH ROW EXECUTE FUNCTION audit.refuse_change();
CREATE TRIGGER audit_event_no_truncate BEFORE TRUNCATE ON audit.audit_event
    FOR EACH STATEMENT EXECUTE FUNCTION audit.refuse_change();
CREATE TRIGGER emergency_review_append_only BEFORE UPDATE OR DELETE ON audit.emergency_review
    FOR EACH ROW EXECUTE FUNCTION audit.refuse_change();
CREATE TRIGGER emergency_review_no_truncate BEFORE TRUNCATE ON audit.emergency_review
    FOR EACH STATEMENT EXECUTE FUNCTION audit.refuse_change();

GRANT USAGE ON SCHEMA audit TO pmp_app;
GRANT SELECT, INSERT ON audit.audit_event, audit.emergency_review TO pmp_app;
GRANT SELECT, UPDATE ON audit.chain_head TO pmp_app;
GRANT SELECT, INSERT ON audit.patient_notification TO pmp_app;
GRANT UPDATE (sent_at) ON audit.patient_notification TO pmp_app;
