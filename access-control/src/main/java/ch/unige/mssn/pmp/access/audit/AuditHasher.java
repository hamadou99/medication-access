package ch.unige.mssn.pmp.access.audit;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Chaîne les événements : chaque empreinte couvre l'événement et l'empreinte précédente, avec une clé hors de la base.
 * Modifier ou retirer un événement casse donc toute la suite, et recalculer la chaîne exige la clé.
 */
public final class AuditHasher {

    private static final String ALGORITHM = "HmacSHA256";

    private final SecretKeySpec key;

    public AuditHasher(byte[] secret) {
        if (secret.length < 32) {
            throw new IllegalArgumentException("La clé du journal doit compter au moins 32 octets");
        }
        this.key = new SecretKeySpec(secret, ALGORITHM);
    }

    public String hash(long seq, String previousHash, AuditEvent event) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return HexFormat.of().formatHex(mac.doFinal(canonical(seq, previousHash, event).getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC indisponible", e);
        }
    }

    /** Chaque champ est précédé de sa longueur, pour que déplacer du texte d'un champ à l'autre change l'empreinte. */
    static String canonical(long seq, String previousHash, AuditEvent e) {
        StringBuilder out = new StringBuilder();
        for (String field : new String[] {String.valueOf(seq), previousHash, e.eventUuid().toString(), e.occurredAt().toString(),
                e.personId(), e.personName(), e.actingRole(), e.patientId(), e.resource(), e.action(), e.atcEvent(), e.outcome(),
                e.reasonCode(), e.reasonText(), e.purposeOfUse(), e.emergencyJustification(), e.reference(), e.policyVersion()}) {
            if (field == null) {
                out.append("-1:");
            } else {
                out.append(field.length()).append(':').append(field);
            }
            out.append('|');
        }
        return out.toString();
    }
}
