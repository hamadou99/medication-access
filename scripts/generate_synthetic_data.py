#!/usr/bin/env python3
"""Génère les données fictives au format CH EMED EPR à partir des exemples officiels du guide 3.0.0 (fhir.ch).

Produit :
- les gabarits MTP, PRE, DIS et PADV utilisés par l'application de démonstration ;
- un document MTP par traitement fictif (data/synthetic/), validable avec le validateur HL7 ;
- la migration SQL qui charge ces traitements.

Usage : generate_synthetic_data.py <dossier contenant Bundle-DocumentMtp1.json, ...Pre1, ...Dis1, ...Padv1>
Toutes les personnes sont inventées. Aucune donnée réelle n'entre dans ce script.
"""
import json
import pathlib
import re
import sys
import uuid

ROOT = pathlib.Path(__file__).resolve().parent.parent
TEMPLATES = ROOT / "demo-app/src/main/resources/fhir"
SEED = ROOT / "demo-app/src/main/resources/db/seed"
DATA = ROOT / "data/synthetic"

ATC_SYSTEM = "http://www.whocc.no/atc"
NAMESPACE = uuid.UUID("6f0b5f3e-0d57-4d3c-9c55-1c9f7a3f2a10")

CONFIDENTIALITY = {
    "NORMAL": ("N", "17621005", "Normal (qualifier value)"),
    "RESTREINT": ("R", "263856008", "Restricted (qualifier value)"),
    "SECRET": ("V", "1141000195107", "Secret (qualifier value)"),
}

PATIENTS = {
    "761337000000000001": ("Dupont", "Jeanne", "female", "1951-03-14"),
    "761337000000000002": ("Morel", "Paul", "male", "1968-11-02"),
    "761337000000000003": ("Favre", "Anne", "female", "1980-06-25"),
    "761337000000000004": ("Rochat", "Luc", "male", "1975-01-30"),
    "761337000000000005": ("Meyer", "Hugo", "male", "1979-09-12"),
}
MARTIN = ("Martin", "Claire", "7601000000001")

TREATMENTS = [
    # patient, ATC, médicament, posologie, niveau, statut
    ("761337000000000001", "C09AA05", "Ramipril 2.5 mg", "Un comprimé le matin", "NORMAL", "active"),
    ("761337000000000001", "C10AA05", "Atorvastatine 20 mg", "Un comprimé le soir", "NORMAL", "active"),
    ("761337000000000001", "N02BE01", "Paracétamol 500 mg", "Un comprimé si douleur, 4 par jour au maximum", "NORMAL", "suspended"),
    ("761337000000000001", "N06AB04", "Citalopram 20 mg", "Un comprimé le matin", "RESTREINT", "active"),
    ("761337000000000001", "J05AR03", "Ténofovir/emtricitabine", "Un comprimé par jour", "SECRET", "active"),
    ("761337000000000002", "A10BA02", "Metformine 500 mg", "Un comprimé matin et soir", "NORMAL", "active"),
    ("761337000000000003", "B01AA04", "Phenprocoumone 3 mg", "Selon le carnet de suivi", "NORMAL", "active"),
    ("761337000000000003", "N05BA06", "Lorazépam 1 mg", "Un comprimé si anxiété", "RESTREINT", "active"),
    ("761337000000000004", "C07AB07", "Bisoprolol 5 mg", "Un comprimé le matin", "NORMAL", "active"),
    ("761337000000000005", "C09AA05", "Ramipril 5 mg", "Un comprimé le matin", "NORMAL", "active"),
]


def strip_narrative(node):
    if isinstance(node, dict):
        return {k: strip_narrative(v) for k, v in node.items()
                if not (k == "text" and isinstance(v, dict) and "div" in v)}
    if isinstance(node, list):
        return [strip_narrative(x) for x in node]
    return node


def resource(bundle, resource_type):
    return next(e["resource"] for e in bundle["entry"] if e["resource"]["resourceType"] == resource_type)


def tokenize_medication(owner):
    medication = owner["contained"][0]
    medication["code"] = {
        "coding": [{"system": ATC_SYSTEM, "code": "{{ATC}}", "display": "{{MEDICATION}}"}],
        "text": "{{MEDICATION}}",
    }
    for optional in ("form", "amount", "ingredient"):
        medication.pop(optional, None)


def tokenize_common(bundle):
    composition = resource(bundle, "Composition")
    document_uuid = bundle["identifier"]["value"]
    bundle.pop("id", None)
    timestamp = bundle["timestamp"]

    patient = resource(bundle, "Patient")
    patient.pop("address", None)
    patient["identifier"][0]["value"] = "{{PATIENT_ID}}"
    patient["name"] = [{"family": "{{PATIENT_FAMILY}}", "given": ["{{PATIENT_GIVEN}}"]}]
    patient["gender"] = "{{PATIENT_GENDER}}"
    patient["birthDate"] = "{{PATIENT_BIRTHDATE}}"

    practitioner = resource(bundle, "Practitioner")
    practitioner["identifier"][0]["value"] = "{{AUTHOR_GLN}}"
    practitioner["name"] = [{"family": "{{AUTHOR_FAMILY}}", "given": ["{{AUTHOR_GIVEN}}"]}]

    composition["confidentiality"] = "{{CONF_CODE}}"
    composition["_confidentiality"]["extension"][0]["valueCodeableConcept"]["coding"][0].update(
        code="{{CONF_SNOMED}}", display="{{CONF_DISPLAY}}")

    text = json.dumps(bundle, ensure_ascii=False, indent=1)
    text = text.replace(document_uuid, "{{DOCUMENT_UUID}}")
    text = text.replace(timestamp, "{{TIMESTAMP}}")
    return text


def load(folder, name):
    return strip_narrative(json.loads((folder / f"Bundle-{name}.json").read_text(encoding="utf-8")))


def mtp_template(folder):
    bundle = load(folder, "DocumentMtp1")
    statement = resource(bundle, "MedicationStatement")
    tokenize_medication(statement)
    treatment_uuid = statement["identifier"][0]["value"]
    statement["status"] = "active"
    statement["dateAsserted"] = "{{DATE}}"
    statement["dosage"] = [{"text": "{{DOSAGE}}"}]
    return tokenize_common(bundle).replace(treatment_uuid, "{{TREATMENT_UUID}}")


def tokenize_item_links(item):
    """Remplace les liens CH EMED d'une entrée par des jetons : traitement, prescription et identifiant propre."""
    for link in item.get("extension", []):
        target = "PRESCRIPTION" if link["url"].endswith("ch-emed-ext-prescription") else "TREATMENT"
        for part in link.get("extension", []):
            suffix = "DOCUMENT_UUID" if part["url"] == "externalDocumentId" else "UUID"
            part["valueIdentifier"]["value"] = "{{%s_%s}}" % (target, suffix)
    item["identifier"][0]["value"] = "{{ITEM_UUID}}"


def pre_template(folder):
    bundle = load(folder, "DocumentPre1")
    request = resource(bundle, "MedicationRequest")
    tokenize_medication(request)
    tokenize_item_links(request)
    request["authoredOn"] = "{{DATE}}"
    request["dosageInstruction"] = [{"text": "{{DOSAGE}}"}]
    return tokenize_common(bundle)


def dis_template(folder):
    bundle = load(folder, "DocumentDis1")
    dispense = resource(bundle, "MedicationDispense")
    tokenize_medication(dispense)
    tokenize_item_links(dispense)
    dispense["whenHandedOver"] = "{{DATE}}"
    dispense["dosageInstruction"] = [{"text": "{{DOSAGE}}"}]
    return tokenize_common(bundle)


def padv_template(folder):
    bundle = load(folder, "DocumentPadv1")
    observation = resource(bundle, "Observation")
    treatment_uuid = observation["extension"][0]["extension"][0]["valueIdentifier"]["value"]
    external_uuid = observation["extension"][0]["extension"][1]["valueIdentifier"]["value"]
    observation["code"]["coding"][0]["code"] = "{{ADVICE_CODE}}"
    observation["note"] = [{"text": "{{NOTE}}"}]
    bundle["entry"] = [e for e in bundle["entry"] if e["resource"]["resourceType"] != "MedicationStatement"]
    observation["extension"] = [e for e in observation["extension"] if not e["url"].endswith("medicationstatement-changed")]
    composition = resource(bundle, "Composition")
    composition["section"] = composition["section"][:1]
    result = tokenize_common(bundle)
    return result.replace(treatment_uuid, "{{TREATMENT_UUID}}").replace(external_uuid, "{{TREATMENT_DOCUMENT_UUID}}")


def render(template, values):
    for key, value in values.items():
        template = template.replace("{{" + key + "}}", json.dumps(value, ensure_ascii=False)[1:-1])
    missing = re.findall(r"\{\{[A-Z_]+\}\}", template)
    if missing:
        raise SystemExit(f"Jetons non remplacés : {sorted(set(missing))}")
    return template


def sql(value):
    return "NULL" if value is None else "'" + value.replace("'", "''") + "'"


def main(folder):
    TEMPLATES.mkdir(parents=True, exist_ok=True)
    SEED.mkdir(parents=True, exist_ok=True)
    DATA.mkdir(parents=True, exist_ok=True)
    mtp = mtp_template(folder)
    for name, text in {"mtp": mtp, "pre": pre_template(folder), "dis": dis_template(folder), "padv": padv_template(folder)}.items():
        (TEMPLATES / f"{name}-template.json").write_text(text, encoding="utf-8")

    lines = ["-- Généré par scripts/generate_synthetic_data.py. Données entièrement fictives."]
    for patient_id, (family, given, gender, birth) in PATIENTS.items():
        lines.append("INSERT INTO plan.patient VALUES (%s, %s, %s, %s, %s);" % tuple(map(sql, (patient_id, family, given, gender, birth))))
    for index, (patient_id, atc, medication, dosage, level, status) in enumerate(TREATMENTS, 1):
        family, given, gender, birth = PATIENTS[patient_id]
        treatment_uuid = str(uuid.uuid5(NAMESPACE, f"treatment-{index}"))
        document_uuid = str(uuid.uuid5(NAMESPACE, f"document-{index}"))
        code, snomed, display = CONFIDENTIALITY[level]
        document = render(mtp, {
            "DOCUMENT_UUID": f"urn:uuid:{document_uuid}", "TREATMENT_UUID": f"urn:uuid:{treatment_uuid}",
            "TIMESTAMP": "2026-10-01T09:00:00+02:00", "DATE": "2026-10-01",
            "PATIENT_ID": patient_id, "PATIENT_FAMILY": family, "PATIENT_GIVEN": given,
            "PATIENT_GENDER": gender, "PATIENT_BIRTHDATE": birth,
            "AUTHOR_FAMILY": MARTIN[0], "AUTHOR_GIVEN": MARTIN[1], "AUTHOR_GLN": MARTIN[2],
            "ATC": atc, "MEDICATION": medication, "DOSAGE": dosage,
            "CONF_CODE": code, "CONF_SNOMED": snomed, "CONF_DISPLAY": display,
        })
        json.loads(document)
        (DATA / f"mtp-{index:02d}.json").write_text(document, encoding="utf-8")
        lines.append("INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES "
                     f"('{treatment_uuid}', '{document_uuid}', {sql(patient_id)}, {sql(atc)}, {sql(medication)}, {sql(dosage)}, {sql(level)}, {sql(status)}, {sql(MARTIN[2])}, '2026-10-01T07:00:00Z');")
        lines.append("INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES "
                     f"('{document_uuid}', '{treatment_uuid}', {sql(patient_id)}, 'MTP', {sql(level)}, {sql(MARTIN[2])}, '2026-10-01T07:00:00Z', {sql(document)}::jsonb);")
    (SEED / "V101__plan_fictif.sql").write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"{len(TREATMENTS)} documents MTP, 4 gabarits")


if __name__ == "__main__":
    main(pathlib.Path(sys.argv[1]))
