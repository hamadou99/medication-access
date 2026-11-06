# language: fr
Fonctionnalité: Droits d'accès accordés par le patient

  @EX-02 @EX-28
  Scénario: Un médecin exclu par la patiente ne l'apprend pas par le message
    Quand "Dr Perrin" consulte le plan de "Mme Dupont"
    Alors la demande est refusée
    Et le message ne mentionne pas "exclu"
    Et le journal garde une trace "DENY" au motif "Professionnel exclu par le patient"

  @EX-02
  Scénario: L'exclusion ferme aussi l'accès d'urgence
    Quand "Dr Perrin" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Alors la demande est refusée
    Et le message ne mentionne pas "exclu"
    Et le journal garde une trace "DENY" au motif "Accès d'urgence fermé pour ce dossier"

  @EX-05
  Scénario: Un droit d'accès expiré n'ouvre plus le dossier
    Quand "Dre Roy" consulte le plan de "Mme Dupont"
    Alors la demande est refusée
    Et le journal garde une trace "DENY" au motif "Droit d'accès expiré ou pas encore valide"

  @EX-05
  Scénario: Un droit retiré s'applique dès la demande suivante
    Soit "Mme Favre" a retiré le droit d'accès de "Dr Keller" au dossier de "Mme Favre"
    Quand "Dr Keller" consulte le plan de "Mme Favre"
    Alors la demande est refusée

  @EX-03 @EX-04
  Scénario: Le médecin au droit élargi voit le niveau restreint, jamais le secret
    Quand "Dre Martin" consulte le plan de "Mme Dupont"
    Alors la demande est acceptée
    Et le niveau accordé est "RESTREINT"
    Et le plan affiché contient "Citalopram 20 mg"
    Et le plan affiché ne contient pas "Ténofovir/emtricitabine"

  @EX-03
  Scénario: Le droit normal ne montre pas le niveau restreint
    Quand "l'infirmière Vidal" consulte le plan de "Mme Dupont"
    Alors la demande est acceptée
    Et le niveau accordé est "NORMAL"
    Et le plan affiché contient "Ramipril 2.5 mg"
    Et le plan affiché ne contient pas "Citalopram 20 mg"

  @EX-03
  Scénario: Le pharmacien au droit normal ne peut pas agir sur une donnée restreinte
    Quand "le pharmacien Gros" enregistre une délivrance du traitement "Citalopram 20 mg" de "Mme Dupont"
    Alors la demande est refusée
    Et le journal garde une trace "DENY" au motif "Niveau de confidentialité supérieur au droit accordé"

  @EX-04
  Scénario: Un médecin ne peut pas créer une donnée secrète
    Quand "Dre Martin" inscrit le traitement "Essai secret" de niveau "SECRET" au plan de "Mme Dupont"
    Alors la demande est refusée

  @EX-10
  Scénario: La patiente voit toutes ses données, y compris secrètes
    Quand "Mme Dupont" consulte le plan de "Mme Dupont"
    Alors la demande est acceptée
    Et le niveau accordé est "SECRET"
    Et le plan affiché contient "Ténofovir/emtricitabine"

  @EX-11
  Scénario: Une représentante au mandat valide consulte le plan
    Quand "Léa Dupont" consulte le plan de "Mme Dupont"
    Alors la demande est acceptée
    Et le journal garde une trace "PERMIT" au motif "Mandat de représentation valide"

  @EX-11
  Scénario: Un mandat révoqué n'ouvre plus le dossier
    Quand "Marc Morel" consulte le plan de "M. Morel"
    Alors la demande est refusée

  @EX-11
  Scénario: Un mandat limité au plan n'ouvre pas le journal
    Quand "Ana Favre" consulte le journal de "Mme Favre"
    Alors la demande est refusée
    Et le journal garde une trace "DENY" au motif "Le mandat ne couvre pas cette action"

  @EX-12
  Scénario: Un consentement révoqué ferme le dossier aux tiers, pas au patient
    Quand "Dre Martin" consulte le plan de "M. Rochat"
    Alors la demande est refusée
    Et le journal garde une trace "DENY" au motif "Consentement du patient révoqué"
    Quand "M. Rochat" consulte le plan de "M. Rochat"
    Alors la demande est acceptée

  @EX-26 @EX-28
  Scénario: Un patient inconnu donne le même message qu'un dossier sans droit
    Quand "le soignant Blanc" consulte le plan de "un patient inconnu"
    Alors la demande est refusée
    Et le message explique "Vous n'avez pas de droit d'accès à ce dossier"
    Et le journal garde une trace "DENY" au motif "Patient inconnu"

  @EX-36
  Scénario: Un médecin agit comme patient sur son propre dossier
    Quand "Dr Meyer" consulte le plan de "Dr Meyer"
    Alors la demande est acceptée
    Et le journal garde une trace "PERMIT" au motif "Accès du patient à ses propres données"
    Quand "Dr Meyer" inscrit le traitement "Auto-prescription" au plan de "Dr Meyer"
    Alors la demande est refusée
