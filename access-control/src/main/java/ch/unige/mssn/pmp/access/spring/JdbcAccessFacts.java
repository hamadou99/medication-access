package ch.unige.mssn.pmp.access.spring;

import ch.unige.mssn.pmp.access.engine.AccessFacts;
import ch.unige.mssn.pmp.access.model.Confidentiality;
import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcAccessFacts implements AccessFacts {

    private final JdbcClient jdbc;

    public JdbcAccessFacts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<PatientSettings> patientSettings(String patientId) {
        return jdbc.sql("SELECT consent_active, emergency_mode FROM access.patient_settings WHERE patient_id = ?")
                .param(patientId)
                .query((rs, n) -> new PatientSettings(rs.getBoolean(1), EmergencyMode.valueOf(rs.getString(2))))
                .optional();
    }

    @Override
    public boolean isExcluded(String personId, String patientId) {
        return jdbc.sql("SELECT count(*) FROM access.access_exclusion WHERE person_id = ? AND patient_id = ?")
                .params(personId, patientId)
                .query(Long.class)
                .single() > 0;
    }

    @Override
    public Optional<Grant> grant(String personId, String patientId) {
        return jdbc.sql("SELECT level, valid_from, valid_until FROM access.access_grant WHERE person_id = ? AND patient_id = ?")
                .params(personId, patientId)
                .query((rs, n) -> new Grant(Confidentiality.valueOf(rs.getString(1)), instant(rs.getTimestamp(2)), instant(rs.getTimestamp(3))))
                .optional();
    }

    @Override
    public Optional<Mandate> mandate(String personId, String patientId) {
        return jdbc.sql("SELECT valid_from, valid_until, revoked, actions FROM access.representation_mandate WHERE person_id = ? AND patient_id = ?")
                .params(personId, patientId)
                .query((rs, n) -> new Mandate(instant(rs.getTimestamp(1)), instant(rs.getTimestamp(2)), rs.getBoolean(3), actions(rs.getArray(4))))
                .optional();
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private static Set<String> actions(Array array) throws SQLException {
        return Set.of((String[]) array.getArray());
    }
}
