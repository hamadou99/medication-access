# language: fr
Fonctionnalité: Journal d'audit et cycle de vie d'un traitement

  @EX-16 @EX-15
  Scénario: La consultation du journal laisse elle-même une trace
    Soit "Mme Dupont" consulte le journal de "Mme Dupont"
    Quand "Mme Dupont" consulte le journal de "Mme Dupont"
    Alors la demande est acceptée
    Et le journal affiché contient l'action "CONSULTER_JOURNAL"

  @EX-15
  Scénario: Une représentante au mandat valide lit le journal
    Quand "Léa Dupont" consulte le journal de "Mme Dupont"
    Alors la demande est acceptée

  @EX-20
  Scénario: Un médecin traitant ne lit pas le journal
    Quand "Dre Martin" consulte le journal de "Mme Dupont"
    Alors la demande est refusée

  @EX-20
  Scénario: La fonction de contrôle ne lit pas les données cliniques
    Quand "l'auditrice Roth" consulte le plan de "Mme Dupont"
    Alors la demande est refusée

  @EX-14 @EX-17
  Scénario: Le journal s'exporte au format d'AuditEvent de CH:ATC
    Soit "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Quand "Mme Dupont" consulte le journal de "Mme Dupont" au format CH:ATC
    Alors la demande est acceptée
    Et la réponse FHIR contient un AuditEvent "ATC_DOC_READ" avec le but "EMER"

  @EX-19
  Scénario: L'auditrice vérifie l'intégrité du journal
    Soit "Dre Martin" consulte le plan de "Mme Dupont"
    Quand "l'auditrice Roth" vérifie l'intégrité du journal
    Alors la demande est acceptée
    Et le journal est déclaré intègre

  @EX-19
  Scénario: Seule la fonction de contrôle vérifie l'intégrité
    Quand "Dre Martin" vérifie l'intégrité du journal
    Alors la demande est refusée

  @EX-22 @EX-23
  Scénario: Le médecin prescrit, le pharmacien délivre, et l'inverse est refusé
    Soit "Dre Martin" a inscrit le traitement "Ibuprofène 400 mg" au plan de "Mme Dupont"
    Quand "le pharmacien Gros" prescrit le traitement "Ibuprofène 400 mg" de "Mme Dupont"
    Alors la demande est refusée
    Quand "Dre Martin" enregistre une délivrance du traitement "Ibuprofène 400 mg" de "Mme Dupont"
    Alors la demande est refusée
    Quand "Dre Martin" prescrit le traitement "Ibuprofène 400 mg" de "Mme Dupont"
    Alors la demande est acceptée
    Et 1 document CH EMED de type "PRE" existe pour le traitement "Ibuprofène 400 mg" de "Mme Dupont"
    Quand "le pharmacien Gros" enregistre une délivrance du traitement "Ibuprofène 400 mg" de "Mme Dupont"
    Alors la demande est acceptée
    Et 1 document CH EMED de type "DIS" existe pour le traitement "Ibuprofène 400 mg" de "Mme Dupont"

  @EX-24
  Scénario: Le pharmacien émet un avis sans changer le traitement
    Soit "Dre Martin" a inscrit le traitement "Oméprazole 20 mg" au plan de "Mme Dupont"
    Quand "le pharmacien Gros" émet un avis pharmaceutique sur le traitement "Oméprazole 20 mg" de "Mme Dupont"
    Alors la demande est acceptée
    Et 1 document CH EMED de type "PADV" existe pour le traitement "Oméprazole 20 mg" de "Mme Dupont"
    Et le traitement "Oméprazole 20 mg" de "Mme Dupont" a le statut "active"

  @EX-24
  Scénario: Le médecin ne remplace pas le pharmacien pour l'avis pharmaceutique
    Soit "Dre Martin" a inscrit le traitement "Vitamine D" au plan de "Mme Dupont"
    Quand "Dre Martin" émet un avis pharmaceutique sur le traitement "Vitamine D" de "Mme Dupont"
    Alors la demande est refusée

  @EX-25 @EX-24
  Scénario: Un traitement suspendu peut reprendre, un traitement annulé non
    Soit "Dre Martin" a inscrit le traitement "Fer 80 mg" au plan de "Mme Dupont"
    Quand "Dre Martin" applique le code "SUSPEND" au traitement "Fer 80 mg" de "Mme Dupont"
    Alors la demande est acceptée
    Et le traitement "Fer 80 mg" de "Mme Dupont" a le statut "suspended"
    Quand "le pharmacien Gros" enregistre une délivrance du traitement "Fer 80 mg" de "Mme Dupont"
    Alors la réponse a le statut 409
    Quand "Dre Martin" applique le code "OK" au traitement "Fer 80 mg" de "Mme Dupont"
    Alors le traitement "Fer 80 mg" de "Mme Dupont" a le statut "active"
    Quand "Dre Martin" applique le code "CANCEL" au traitement "Fer 80 mg" de "Mme Dupont"
    Alors le traitement "Fer 80 mg" de "Mme Dupont" a le statut "cancelled"
    Quand "Dre Martin" applique le code "OK" au traitement "Fer 80 mg" de "Mme Dupont"
    Alors la réponse a le statut 409
