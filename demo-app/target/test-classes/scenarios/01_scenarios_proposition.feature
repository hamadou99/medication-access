# language: fr
Fonctionnalité: Les six scénarios de la proposition

  @EX-01 @EX-13 @EX-14 @EX-21 @EX-27
  Scénario: La Dre Martin, médecin traitant de Mme Dupont, ajoute un traitement à son plan
    Quand "Dre Martin" inscrit le traitement "Amlodipine 5 mg" au plan de "Mme Dupont"
    Alors la demande est acceptée
    Et le journal garde une trace "PERMIT" au motif "Relation de soins établie"
    Et cette trace désigne "Dre Martin", "MEDECIN" et l'action "INSCRIRE_TRAITEMENT"
    Et 1 document CH EMED de type "MTP" existe pour le traitement "Amlodipine 5 mg" de "Mme Dupont"

  @EX-24 @EX-13 @EX-28
  Scénario: Un pharmacien tente de modifier une prescription
    Quand "le pharmacien Gros" applique le code "CHANGE" au traitement "Ramipril 2.5 mg" de "Mme Dupont"
    Alors la demande est refusée
    Et le message explique "Votre rôle ne permet pas cette action"
    Et le journal garde une trace "DENY" au motif "Le rôle ne permet pas cette action"
    Et le traitement "Ramipril 2.5 mg" de "Mme Dupont" a le statut "active"

  @EX-11 @EX-13
  Scénario: Le fils de Mme Dupont consulte le plan alors que son mandat a expiré
    Quand "Thomas Dupont" consulte le plan de "Mme Dupont"
    Alors la demande est refusée
    Et le message explique "mandat de représentation n'est pas valide"
    Et le journal garde une trace "DENY" au motif "Mandat de représentation expiré, révoqué ou pas encore valide"

  @EX-06 @EX-09 @EX-17 @EX-33
  Scénario: Un médecin des urgences, sans lien préalable, consulte le plan d'une patiente inconsciente
    Quand "Dr Keller" demande l'accès d'urgence au plan de "Mme Dupont" avec la justification "Patiente inconsciente, admise aux urgences"
    Alors la demande est acceptée au titre de l'urgence
    Et le plan affiché contient "Ramipril 2.5 mg"
    Et le niveau accordé est "NORMAL"
    Et le journal garde une trace "EMERGENCY_PERMIT" au motif "Accès d'urgence (bris de glace), à vérifier"
    Et cette trace indique l'urgence avec la justification "Patiente inconsciente, admise aux urgences"
    Et "Mme Dupont" a une information en attente

  @EX-10 @EX-15 @EX-16
  Scénario: Mme Dupont consulte qui a accédé à son plan ce mois-ci
    Soit "Dre Martin" consulte le plan de "Mme Dupont"
    Quand "Mme Dupont" consulte le journal de "Mme Dupont"
    Alors la demande est acceptée
    Et le journal affiché contient une trace "PERMIT" de "Dre Martin"

  @EX-01 @EX-13 @EX-15 @EX-28
  Scénario: Un soignant tente de consulter le plan d'un voisin, sans lien de soins
    Quand "le soignant Blanc" consulte le plan de "Mme Dupont"
    Alors la demande est refusée
    Et le journal garde une trace "DENY" au motif "Aucune relation de soins ni mandat avec ce patient"
    Quand "Mme Dupont" consulte le journal de "Mme Dupont"
    Alors le journal affiché contient une trace "DENY" de "le soignant Blanc"
