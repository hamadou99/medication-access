package ch.unige.mssn.pmp.access.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Soumet une méthode au contrôle d'accès et au journal. Les attributs textuels sont des expressions SpEL évaluées
 * sur les paramètres de la méthode, par exemple {@code patient = "#patientId"}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessControlled {

    String resource();

    String action();

    /** Identifiant du patient visé. Vide pour une action globale. */
    String patient() default "";

    /** Justification saisie pour un accès d'urgence. */
    String emergencyJustification() default "";

    /** Niveau de confidentialité de la donnée visée ({@code Confidentiality}). */
    String dataLevel() default "";

    /** Donnée visée, reprise dans le journal. */
    String reference() default "";
}
