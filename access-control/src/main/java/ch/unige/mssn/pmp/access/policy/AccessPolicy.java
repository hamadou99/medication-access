package ch.unige.mssn.pmp.access.policy;

import ch.unige.mssn.pmp.access.model.Role;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Tableau de règles chargé depuis un ou plusieurs fichiers YAML. Toute action absente est refusée. Une erreur dans
 * un fichier empêche le démarrage : mieux vaut une application arrêtée qu'une règle mal comprise.
 */
public final class AccessPolicy {

    private final String version;
    private final Map<String, ActionRule> rules;

    private AccessPolicy(String version, Map<String, ActionRule> rules) {
        this.version = version;
        this.rules = rules;
    }

    public String version() {
        return version;
    }

    public Optional<ActionRule> rule(String resource, String action) {
        return Optional.ofNullable(rules.get(key(resource, action)));
    }

    public Collection<ActionRule> rules() {
        return rules.values();
    }

    public static AccessPolicy load(InputStream... sources) {
        String version = null;
        Map<String, ActionRule> rules = new LinkedHashMap<>();
        for (InputStream source : sources) {
            Map<String, Object> root = parse(source);
            String fileVersion = text(root, "version");
            version = version == null ? fileVersion : version + "+" + fileVersion;
            if (!(root.get("resources") instanceof Map<?, ?> resources)) {
                throw new PolicyException("Champ obligatoire manquant ou invalide : resources");
            }
            resources.forEach((resource, actions) -> readActions(String.valueOf(resource), actions, rules));
        }
        if (rules.isEmpty()) {
            throw new PolicyException("Le tableau de règles est vide");
        }
        return new AccessPolicy(version, Map.copyOf(rules));
    }

    private static Map<String, Object> parse(InputStream source) {
        try (source; var reader = new InputStreamReader(source, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
            if (loaded instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> root = (Map<String, Object>) loaded;
                return root;
            }
            throw new PolicyException("Le fichier de règles doit contenir un document YAML");
        } catch (IOException e) {
            throw new PolicyException("Lecture du fichier de règles impossible : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void readActions(String resource, Object actions, Map<String, ActionRule> rules) {
        if (!(actions instanceof Map<?, ?>)) {
            throw new PolicyException(resource + " : liste d'actions invalide");
        }
        ((Map<String, Object>) actions).forEach((action, raw) -> {
            String key = key(resource, action);
            if (!(raw instanceof Map<?, ?>)) {
                throw new PolicyException(key + " : définition invalide");
            }
            Map<String, Object> def = (Map<String, Object>) raw;
            ActionRule rule = new ActionRule(resource, action, readRoles(key, def),
                    flag(def, "emergency"), flag(def, "global"), flag(def, "levelBased"),
                    text(def, "atcEvent"), (String) def.get("chEmedDocument"));
            if (rule.emergency() && rule.global()) {
                throw new PolicyException(key + " : le bris de glace exige un patient");
            }
            if (rule.emergency() && rule.roles().stream().noneMatch(Role::isProfessional)) {
                throw new PolicyException(key + " : le bris de glace exige un rôle professionnel");
            }
            if (rules.put(key, rule) != null) {
                throw new PolicyException(key + " : règle en double");
            }
        });
    }

    private static Set<Role> readRoles(String key, Map<String, Object> def) {
        if (!(def.get("roles") instanceof List<?> names)) {
            throw new PolicyException(key + " : la liste des rôles est obligatoire");
        }
        Set<Role> roles = EnumSet.noneOf(Role.class);
        for (Object name : names) {
            try {
                roles.add(Role.valueOf(String.valueOf(name)));
            } catch (IllegalArgumentException e) {
                throw new PolicyException(key + " : rôle inconnu " + name);
            }
        }
        return roles;
    }

    private static boolean flag(Map<String, Object> def, String name) {
        return Boolean.TRUE.equals(def.get(name));
    }

    private static String text(Map<String, Object> def, String name) {
        if (def.get(name) instanceof String value && !value.isBlank()) {
            return value;
        }
        throw new PolicyException("Champ obligatoire manquant ou invalide : " + name);
    }

    private static String key(String resource, String action) {
        return resource + "/" + action;
    }

    public static final class PolicyException extends RuntimeException {
        public PolicyException(String message) {
            super(message);
        }
    }
}
