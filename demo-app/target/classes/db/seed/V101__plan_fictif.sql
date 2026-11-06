-- Généré par scripts/generate_synthetic_data.py. Données entièrement fictives.
INSERT INTO plan.patient VALUES ('761337000000000001', 'Dupont', 'Jeanne', 'female', '1951-03-14');
INSERT INTO plan.patient VALUES ('761337000000000002', 'Morel', 'Paul', 'male', '1968-11-02');
INSERT INTO plan.patient VALUES ('761337000000000003', 'Favre', 'Anne', 'female', '1980-06-25');
INSERT INTO plan.patient VALUES ('761337000000000004', 'Rochat', 'Luc', 'male', '1975-01-30');
INSERT INTO plan.patient VALUES ('761337000000000005', 'Meyer', 'Hugo', 'male', '1979-09-12');
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('a89aa429-cfc5-53c9-b48d-4fc80609c1e8', '1d2eadea-838d-52d3-ba6e-a3d2bb5cfeeb', '761337000000000001', 'C09AA05', 'Ramipril 2.5 mg', 'Un comprimé le matin', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('1d2eadea-838d-52d3-ba6e-a3d2bb5cfeeb', 'a89aa429-cfc5-53c9-b48d-4fc80609c1e8', '761337000000000001', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:1d2eadea-838d-52d3-ba6e-a3d2bb5cfeeb"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:1d2eadea-838d-52d3-ba6e-a3d2bb5cfeeb",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:1d2eadea-838d-52d3-ba6e-a3d2bb5cfeeb"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000001"
     }
    ],
    "name": [
     {
      "family": "Dupont",
      "given": [
       "Jeanne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1951-03-14"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "C09AA05",
         "display": "Ramipril 2.5 mg"
        }
       ],
       "text": "Ramipril 2.5 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:a89aa429-cfc5-53c9-b48d-4fc80609c1e8"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé le matin"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('9d6c777d-d4ca-559b-b896-eb56a3fa7ef5', 'f2cfa710-7ec6-51fc-b156-023e5d2a5c46', '761337000000000001', 'C10AA05', 'Atorvastatine 20 mg', 'Un comprimé le soir', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('f2cfa710-7ec6-51fc-b156-023e5d2a5c46', '9d6c777d-d4ca-559b-b896-eb56a3fa7ef5', '761337000000000001', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:f2cfa710-7ec6-51fc-b156-023e5d2a5c46"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:f2cfa710-7ec6-51fc-b156-023e5d2a5c46",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:f2cfa710-7ec6-51fc-b156-023e5d2a5c46"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000001"
     }
    ],
    "name": [
     {
      "family": "Dupont",
      "given": [
       "Jeanne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1951-03-14"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "C10AA05",
         "display": "Atorvastatine 20 mg"
        }
       ],
       "text": "Atorvastatine 20 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:9d6c777d-d4ca-559b-b896-eb56a3fa7ef5"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé le soir"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('2b63d590-f25e-5cf6-8ce0-7414763c08d7', 'e51eb117-89cd-5597-84df-f86027a5adb4', '761337000000000001', 'N02BE01', 'Paracétamol 500 mg', 'Un comprimé si douleur, 4 par jour au maximum', 'NORMAL', 'suspended', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('e51eb117-89cd-5597-84df-f86027a5adb4', '2b63d590-f25e-5cf6-8ce0-7414763c08d7', '761337000000000001', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:e51eb117-89cd-5597-84df-f86027a5adb4"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:e51eb117-89cd-5597-84df-f86027a5adb4",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:e51eb117-89cd-5597-84df-f86027a5adb4"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000001"
     }
    ],
    "name": [
     {
      "family": "Dupont",
      "given": [
       "Jeanne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1951-03-14"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "N02BE01",
         "display": "Paracétamol 500 mg"
        }
       ],
       "text": "Paracétamol 500 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:2b63d590-f25e-5cf6-8ce0-7414763c08d7"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé si douleur, 4 par jour au maximum"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('0b8199e6-b5f8-518c-b950-079f37c35bf0', '971ac676-0914-502b-bee1-83a67d12457e', '761337000000000001', 'N06AB04', 'Citalopram 20 mg', 'Un comprimé le matin', 'RESTREINT', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('971ac676-0914-502b-bee1-83a67d12457e', '0b8199e6-b5f8-518c-b950-079f37c35bf0', '761337000000000001', 'MTP', 'RESTREINT', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:971ac676-0914-502b-bee1-83a67d12457e"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:971ac676-0914-502b-bee1-83a67d12457e",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:971ac676-0914-502b-bee1-83a67d12457e"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "R",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "263856008",
          "display": "Restricted (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000001"
     }
    ],
    "name": [
     {
      "family": "Dupont",
      "given": [
       "Jeanne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1951-03-14"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "N06AB04",
         "display": "Citalopram 20 mg"
        }
       ],
       "text": "Citalopram 20 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:0b8199e6-b5f8-518c-b950-079f37c35bf0"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé le matin"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('de4e7bde-d0e9-5a14-a901-123823dbcf21', '4444317a-7149-5465-970f-6621ae915cf2', '761337000000000001', 'J05AR03', 'Ténofovir/emtricitabine', 'Un comprimé par jour', 'SECRET', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('4444317a-7149-5465-970f-6621ae915cf2', 'de4e7bde-d0e9-5a14-a901-123823dbcf21', '761337000000000001', 'MTP', 'SECRET', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:4444317a-7149-5465-970f-6621ae915cf2"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:4444317a-7149-5465-970f-6621ae915cf2",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:4444317a-7149-5465-970f-6621ae915cf2"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "V",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "1141000195107",
          "display": "Secret (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000001"
     }
    ],
    "name": [
     {
      "family": "Dupont",
      "given": [
       "Jeanne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1951-03-14"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "J05AR03",
         "display": "Ténofovir/emtricitabine"
        }
       ],
       "text": "Ténofovir/emtricitabine"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:de4e7bde-d0e9-5a14-a901-123823dbcf21"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé par jour"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('b85bc1af-408d-5972-b065-e340f7f88c4d', 'ac6f2fc3-7ece-567b-87be-1081d096dfea', '761337000000000002', 'A10BA02', 'Metformine 500 mg', 'Un comprimé matin et soir', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('ac6f2fc3-7ece-567b-87be-1081d096dfea', 'b85bc1af-408d-5972-b065-e340f7f88c4d', '761337000000000002', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:ac6f2fc3-7ece-567b-87be-1081d096dfea"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:ac6f2fc3-7ece-567b-87be-1081d096dfea",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:ac6f2fc3-7ece-567b-87be-1081d096dfea"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000002"
     }
    ],
    "name": [
     {
      "family": "Morel",
      "given": [
       "Paul"
      ]
     }
    ],
    "gender": "male",
    "birthDate": "1968-11-02"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "A10BA02",
         "display": "Metformine 500 mg"
        }
       ],
       "text": "Metformine 500 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:b85bc1af-408d-5972-b065-e340f7f88c4d"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé matin et soir"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('ad88c935-da44-539e-a68f-1df786c6754e', 'f2c57b27-911d-546c-b384-3b726ef7aa15', '761337000000000003', 'B01AA04', 'Phenprocoumone 3 mg', 'Selon le carnet de suivi', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('f2c57b27-911d-546c-b384-3b726ef7aa15', 'ad88c935-da44-539e-a68f-1df786c6754e', '761337000000000003', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:f2c57b27-911d-546c-b384-3b726ef7aa15"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:f2c57b27-911d-546c-b384-3b726ef7aa15",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:f2c57b27-911d-546c-b384-3b726ef7aa15"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000003"
     }
    ],
    "name": [
     {
      "family": "Favre",
      "given": [
       "Anne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1980-06-25"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "B01AA04",
         "display": "Phenprocoumone 3 mg"
        }
       ],
       "text": "Phenprocoumone 3 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:ad88c935-da44-539e-a68f-1df786c6754e"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Selon le carnet de suivi"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('ac1c153c-ba4d-5741-8bda-15d2385e62c8', '4a5ff9c8-7884-5b45-8df4-747265127a0b', '761337000000000003', 'N05BA06', 'Lorazépam 1 mg', 'Un comprimé si anxiété', 'RESTREINT', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('4a5ff9c8-7884-5b45-8df4-747265127a0b', 'ac1c153c-ba4d-5741-8bda-15d2385e62c8', '761337000000000003', 'MTP', 'RESTREINT', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:4a5ff9c8-7884-5b45-8df4-747265127a0b"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:4a5ff9c8-7884-5b45-8df4-747265127a0b",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:4a5ff9c8-7884-5b45-8df4-747265127a0b"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "R",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "263856008",
          "display": "Restricted (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000003"
     }
    ],
    "name": [
     {
      "family": "Favre",
      "given": [
       "Anne"
      ]
     }
    ],
    "gender": "female",
    "birthDate": "1980-06-25"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "N05BA06",
         "display": "Lorazépam 1 mg"
        }
       ],
       "text": "Lorazépam 1 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:ac1c153c-ba4d-5741-8bda-15d2385e62c8"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé si anxiété"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('0adfae0c-1563-5c00-bc45-a05cfc4b76ce', '42ee4dc9-2066-5ee1-a312-7be67eb9d703', '761337000000000004', 'C07AB07', 'Bisoprolol 5 mg', 'Un comprimé le matin', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('42ee4dc9-2066-5ee1-a312-7be67eb9d703', '0adfae0c-1563-5c00-bc45-a05cfc4b76ce', '761337000000000004', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:42ee4dc9-2066-5ee1-a312-7be67eb9d703"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:42ee4dc9-2066-5ee1-a312-7be67eb9d703",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:42ee4dc9-2066-5ee1-a312-7be67eb9d703"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000004"
     }
    ],
    "name": [
     {
      "family": "Rochat",
      "given": [
       "Luc"
      ]
     }
    ],
    "gender": "male",
    "birthDate": "1975-01-30"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "C07AB07",
         "display": "Bisoprolol 5 mg"
        }
       ],
       "text": "Bisoprolol 5 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:0adfae0c-1563-5c00-bc45-a05cfc4b76ce"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé le matin"
     }
    ]
   }
  }
 ]
}'::jsonb);
INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES ('0b39aecd-d10f-5b33-9c30-c76872953114', 'e805b06b-794d-54be-a54b-ce25275f1142', '761337000000000005', 'C09AA05', 'Ramipril 5 mg', 'Un comprimé le matin', 'NORMAL', 'active', '7601000000001', '2026-10-01T07:00:00Z');
INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES ('e805b06b-794d-54be-a54b-ce25275f1142', '0b39aecd-d10f-5b33-9c30-c76872953114', '761337000000000005', 'MTP', 'NORMAL', '7601000000001', '2026-10-01T07:00:00Z', '{
 "resourceType": "Bundle",
 "meta": {
  "profile": [
   "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-document-medicationtreatmentplan"
  ]
 },
 "identifier": {
  "system": "urn:ietf:rfc:3986",
  "value": "urn:uuid:e805b06b-794d-54be-a54b-ce25275f1142"
 },
 "type": "document",
 "timestamp": "2026-10-01T09:00:00+02:00",
 "entry": [
  {
   "fullUrl": "urn:uuid:e805b06b-794d-54be-a54b-ce25275f1142",
   "resource": {
    "resourceType": "Composition",
    "id": "CompositionMtp1",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-composition-medicationtreatmentplan"
     ]
    },
    "language": "fr-CH",
    "identifier": {
     "system": "urn:ietf:rfc:3986",
     "value": "urn:uuid:e805b06b-794d-54be-a54b-ce25275f1142"
    },
    "status": "final",
    "type": {
     "coding": [
      {
       "system": "http://snomed.info/sct",
       "code": "761931002",
       "display": "Medication treatment plan report (record artifact)"
      },
      {
       "system": "http://loinc.org",
       "code": "77603-9",
       "display": "Medication treatment plan.extended Document"
      }
     ]
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "date": "2026-10-01T09:00:00+02:00",
    "author": [
     {
      "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
     }
    ],
    "title": "Décision thérapeutique relative à la médication",
    "confidentiality": "N",
    "_confidentiality": {
     "extension": [
      {
       "url": "http://fhir.ch/ig/ch-core/StructureDefinition/ch-ext-epr-confidentialitycode",
       "valueCodeableConcept": {
        "coding": [
         {
          "system": "http://snomed.info/sct",
          "code": "17621005",
          "display": "Normal (qualifier value)"
         }
        ]
       }
      }
     ]
    },
    "section": [
     {
      "title": "Plan de traitement médicamenteux",
      "code": {
       "coding": [
        {
         "system": "http://loinc.org",
         "code": "77604-7",
         "display": "Medication treatment plan.brief Document"
        }
       ]
      },
      "entry": [
       {
        "reference": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994"
       }
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3",
   "resource": {
    "resourceType": "Patient",
    "id": "PatientDupont",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-patient"
     ]
    },
    "identifier": [
     {
      "type": {
       "coding": [
        {
         "system": "http://terminology.hl7.org/CodeSystem/v2-0203",
         "code": "MR"
        }
       ]
      },
      "system": "urn:oid:2.999.1",
      "value": "761337000000000005"
     }
    ],
    "name": [
     {
      "family": "Meyer",
      "given": [
       "Hugo"
      ]
     }
    ],
    "gender": "male",
    "birthDate": "1979-09-12"
   }
  },
  {
   "fullUrl": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf",
   "resource": {
    "resourceType": "Practitioner",
    "id": "PractitionerCox",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitioner"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000000001"
     }
    ],
    "name": [
     {
      "family": "Martin",
      "given": [
       "Claire"
      ]
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c",
   "resource": {
    "resourceType": "PractitionerRole",
    "id": "PractitionerRoleCoxAtHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-practitionerrole"
     ]
    },
    "practitioner": {
     "reference": "urn:uuid:9fc8530b-b77d-4b53-8a21-fc786b697edf"
    },
    "organization": {
     "reference": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6"
    }
   }
  },
  {
   "fullUrl": "urn:uuid:580fbe82-8734-4edf-ad4b-48124cdd03c6",
   "resource": {
    "resourceType": "Organization",
    "id": "OrganizationHug",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-organization"
     ]
    },
    "identifier": [
     {
      "system": "urn:oid:2.51.1.3",
      "value": "7601000234438"
     }
    ],
    "name": "Hôpitaux universitaires de Genève",
    "address": [
     {
      "line": [
       "Rue Gabrielle-Perret-Gentil 4"
      ],
      "city": "Genève",
      "postalCode": "1205",
      "country": "CH"
     }
    ]
   }
  },
  {
   "fullUrl": "urn:uuid:e1b5e341-d1a4-4a44-adad-7949974de994",
   "resource": {
    "resourceType": "MedicationStatement",
    "id": "MedicationStatementTriatecMtp",
    "meta": {
     "profile": [
      "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medicationstatement-treatmentplan"
     ]
    },
    "contained": [
     {
      "resourceType": "Medication",
      "id": "MedicationTriatec",
      "meta": {
       "profile": [
        "http://fhir.ch/ig/ch-emed-epr/StructureDefinition/ch-emed-epr-medication"
       ]
      },
      "code": {
       "coding": [
        {
         "system": "http://www.whocc.no/atc",
         "code": "C09AA05",
         "display": "Ramipril 5 mg"
        }
       ],
       "text": "Ramipril 5 mg"
      }
     }
    ],
    "identifier": [
     {
      "system": "urn:ietf:rfc:3986",
      "value": "urn:uuid:0b39aecd-d10f-5b33-9c30-c76872953114"
     }
    ],
    "status": "active",
    "medicationReference": {
     "reference": "#MedicationTriatec"
    },
    "subject": {
     "reference": "urn:uuid:2dbfe659-07d0-45c7-b8df-4a48372049a3"
    },
    "dateAsserted": "2026-10-01",
    "informationSource": {
     "reference": "urn:uuid:213d609a-1164-459a-bb10-727516ae3d0c"
    },
    "dosage": [
     {
      "text": "Un comprimé le matin"
     }
    ]
   }
  }
 ]
}'::jsonb);
