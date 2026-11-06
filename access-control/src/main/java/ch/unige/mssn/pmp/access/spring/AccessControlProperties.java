package ch.unige.mssn.pmp.access.spring;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param auditKey         clé de la chaîne du journal, conservée hors de la base
 * @param policyLocations  fichiers du tableau de règles ; en ajouter un suffit pour couvrir un nouveau type de données
 */
@ConfigurationProperties("pmp.access")
public record AccessControlProperties(String auditKey,
                                      @DefaultValue("classpath:access-policy.yaml") List<String> policyLocations) {
}
