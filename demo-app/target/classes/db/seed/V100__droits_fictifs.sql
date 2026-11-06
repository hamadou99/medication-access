-- Droits de démonstration. Toutes les personnes et tous les identifiants sont inventés.
INSERT INTO access.patient_settings (patient_id, consent_active, emergency_mode) VALUES
    ('761337000000000001', true,  'NORMAL_ONLY'),            -- Mme Dupont
    ('761337000000000002', true,  'DISABLED'),               -- M. Morel : a fermé l'accès d'urgence
    ('761337000000000003', true,  'NORMAL_AND_RESTRICTED'),  -- Mme Favre : a étendu l'accès d'urgence
    ('761337000000000004', false, 'NORMAL_ONLY'),            -- M. Rochat : a révoqué son consentement
    ('761337000000000005', true,  'NORMAL_ONLY');            -- Dr Meyer, patient de son côté

INSERT INTO access.access_grant (person_id, patient_id, level, valid_from, valid_until) VALUES
    ('7601000000001', '761337000000000001', 'RESTREINT', '2025-01-01Z', NULL),          -- Dre Martin, médecin traitant
    ('7601000000001', '761337000000000002', 'NORMAL',    '2025-01-01Z', NULL),
    ('7601000000001', '761337000000000004', 'NORMAL',    '2025-01-01Z', NULL),
    ('7601000000002', '761337000000000001', 'NORMAL',    '2025-01-01Z', NULL),          -- Pierre Gros, pharmacien
    ('7601000000006', '761337000000000001', 'NORMAL',    '2020-01-01Z', '2021-01-01Z'), -- Dre Roy : droit expiré
    ('7601000000007', '761337000000000001', 'NORMAL',    '2025-01-01Z', NULL),          -- Inès Vidal, infirmière du suivi
    ('7601000000008', '761337000000000005', 'NORMAL',    '2025-01-01Z', NULL);          -- Dr Meyer sur son propre dossier

INSERT INTO access.access_exclusion (person_id, patient_id) VALUES
    ('7601000000005', '761337000000000001');                                            -- Dr Perrin, exclu par Mme Dupont

INSERT INTO access.representation_mandate (person_id, patient_id, valid_from, valid_until, revoked, actions) VALUES
    ('REP-THOMAS-DUPONT', '761337000000000001', '2022-01-01Z', '2024-01-01Z', false, ARRAY['CONSULTER_PLAN', 'CONSULTER_JOURNAL']), -- fils : mandat expiré
    ('REP-LEA-DUPONT',    '761337000000000001', '2025-01-01Z', NULL,          false, ARRAY['CONSULTER_PLAN', 'CONSULTER_JOURNAL']), -- fille : mandat valide
    ('REP-MARC-MOREL',    '761337000000000002', '2025-01-01Z', NULL,          true,  ARRAY['CONSULTER_PLAN']),                      -- mandat révoqué
    ('REP-ANA-FAVRE',     '761337000000000003', '2025-01-01Z', NULL,          false, ARRAY['CONSULTER_PLAN']);                      -- sans accès au journal
