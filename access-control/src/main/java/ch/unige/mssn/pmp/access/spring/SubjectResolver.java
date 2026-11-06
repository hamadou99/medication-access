package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.model.Role;
import ch.unige.mssn.pmp.access.model.Subject;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Traduit le jeton en personne connue du module. Un rôle inconnu est ignoré, jamais interprété. */
public class SubjectResolver {

    public static final String PERSON_CLAIM = "pmp_person_id";
    public static final String PATIENT_CLAIM = "epr_spid";

    public Subject current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return new Subject(jwt.getClaimAsString(PERSON_CLAIM), jwt.getClaimAsString(PATIENT_CLAIM),
                    jwt.getClaimAsString("name"), roles(jwt));
        }
        return new Subject(null, null, null, Set.of());
    }

    private static Set<Role> roles(Jwt jwt) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> names) {
            for (Role role : Role.values()) {
                if (names.contains(role.name())) {
                    roles.add(role);
                }
            }
        }
        return roles;
    }
}
