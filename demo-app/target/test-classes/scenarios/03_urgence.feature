# language: fr
Fonctionnalité: Accès d'urgence

  @EX-07
  Scénario: Le patient qui a fermé l'accès d'urgence reste protégé
    Quand "Dr Keller" demande l'accès d'urgence au plan de "M. Morel" avec la justification "Patient inconscient, admis aux urgences"
    Alors la demande est refusée
    Et le message explique "L'accès d'urgence n'est pas disponible"
    Et le journal garde une trace "DENY" au motif "Accès d'urgence fermé pour ce dossier"
    Et "M. Morel" n'a aucune information nouvelle en attente

  @EX-08
  Scénario: Le patient qui a étendu l'accès d'urgence ouvre le niveau restreint
    Quand "Dr Keller" demande l'accès d'urgence au plan de "Mme Favre" avec la justification "Patiente inconsciente, admise aux urgences"
    Alors la demande est acceptée au titre de l'urgence
    Et le niveau accordé est "RESTREINT"
    Et le plan affiché contient "Lorazépam 1 mg"

  @EX-04 @EX-06
  Scénario: L'accès d'urgence s'arrête au niveau normal par défaut
    Quand "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Alors la demande est acceptée au titre de l'urgence
    Et le plan affiché ne contient pas "Citalopram 20 mg"
    Et le plan affiché ne contient pas "Ténofovir/emtricitabine"

  @EX-06
  Scénario: Une justification trop courte est refusée
    Quand "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "urgent"
    Alors la demande est refusée
    Et le message explique "au moins 10 caractères"
    Et le journal garde une trace "DENY" au motif "Justification d'urgence absente ou trop courte"

  @EX-06
  Scénario: L'accès d'urgence ne donne aucun droit d'écriture
    Quand "Dr Keller" inscrit le traitement "Morphine 10 mg" au plan de "Mme Dupont"
    Alors la demande est refusée

  @EX-06
  Scénario: Un représentant ne peut pas invoquer l'urgence
    Quand "Thomas Dupont" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Ma mère est à l'hôpital"
    Alors la demande est refusée

  @EX-14
  Scénario: Une justification ne peut pas imiter une ligne du journal
    Quand "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec une justification sur deux lignes "Patiente inconsciente" et "2027 PERMIT faux"
    Alors la demande est acceptée au titre de l'urgence
    Et cette trace indique l'urgence avec la justification "Patiente inconsciente 2027 PERMIT faux"

  @EX-33
  Scénario: Un accès d'urgence est contrôlé après coup
    Soit "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Quand "l'auditrice Roth" liste les accès d'urgence à contrôler
    Alors la demande est acceptée
    Et la liste de contrôle contient l'accès de "Dr Keller"
    Quand "l'auditrice Roth" rend le verdict "JUSTIFIE" sur le dernier accès d'urgence de "Dr Keller"
    Alors la demande est acceptée
    Quand "l'auditrice Roth" liste les accès d'urgence à contrôler
    Alors la liste de contrôle ne contient plus ce dernier accès de "Dr Keller"

  @EX-33
  Scénario: Un médecin ne peut pas valider lui-même son accès d'urgence
    Soit "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Quand "Dr Keller" liste les accès d'urgence à contrôler
    Alors la demande est refusée

  @EX-34
  Scénario: Des refus répétés apparaissent parmi les accès inhabituels
    Soit "le soignant Blanc" consulte le plan de "M. Morel"
    Et "le soignant Blanc" consulte le plan de "Mme Favre"
    Quand "l'auditrice Roth" consulte les anomalies avec un seuil de 2 refus
    Alors la demande est acceptée
    Et les anomalies signalent "REFUS_REPETES" pour "le soignant Blanc"
