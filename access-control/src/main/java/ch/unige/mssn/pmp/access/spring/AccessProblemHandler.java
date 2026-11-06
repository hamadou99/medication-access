package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.model.ReasonCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Message explicatif pour le demandeur. Le motif détaillé reste dans le journal, retrouvable par la référence. */
@RestControllerAdvice
public class AccessProblemHandler {

    @ExceptionHandler(AccessRefusedException.class)
    ProblemDetail refused(AccessRefusedException refused) {
        ReasonCode reason = refused.decision().reason();
        HttpStatus status = reason == ReasonCode.DECISION_ERROR ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.FORBIDDEN;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, reason.publicMessage());
        problem.setTitle(status == HttpStatus.FORBIDDEN ? "Accès refusé" : "Accès impossible à vérifier");
        problem.setProperty("reference", refused.eventUuid());
        return problem;
    }

    @ExceptionHandler(AuditUnavailableException.class)
    ProblemDetail auditUnavailable(AuditUnavailableException unavailable) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Le journal d'audit est indisponible : aucune action ne peut être effectuée sans trace.");
        problem.setTitle("Accès impossible à tracer");
        return problem;
    }
}
