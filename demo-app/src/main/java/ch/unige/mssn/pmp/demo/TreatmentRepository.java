package ch.unige.mssn.pmp.demo;

import ch.unige.mssn.pmp.access.model.Confidentiality;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository("treatments")
public class TreatmentRepository {

    private final JdbcClient jdbc;

    public TreatmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record Treatment(UUID id, UUID documentId, String patientId, String atc, String medication, String dosage,
                            Confidentiality level, String status) {}

    public List<Treatment> visibleFor(String patientId, Confidentiality clearance) {
        return jdbc.sql("SELECT id, document_id, patient_id, atc, medication, dosage, level, status FROM plan.treatment WHERE patient_id = ? ORDER BY created_at, id")
                .param(patientId)
                .query(TreatmentRepository::treatment)
                .stream()
                .filter(treatment -> treatment.level().atMost(clearance))
                .toList();
    }

    public Optional<Treatment> find(String patientId, UUID id) {
        return jdbc.sql("SELECT id, document_id, patient_id, atc, medication, dosage, level, status FROM plan.treatment WHERE patient_id = ? AND id = ?")
                .params(patientId, id)
                .query(TreatmentRepository::treatment)
                .optional();
    }

    /** Niveau de la donnée visée, lu avant la décision ; {@code null} si elle n'existe pas pour ce patient. */
    public Confidentiality level(String patientId, UUID id) {
        return find(patientId, id).map(Treatment::level).orElse(null);
    }

    public Optional<ChEmedDocuments.Patient> patient(String id) {
        return jdbc.sql("SELECT id, family, given, gender, birth_date FROM plan.patient WHERE id = ?")
                .param(id)
                .query((rs, n) -> new ChEmedDocuments.Patient(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)))
                .optional();
    }

    public void insert(Treatment t, String author, java.time.Instant at) {
        jdbc.sql("INSERT INTO plan.treatment (id, document_id, patient_id, atc, medication, dosage, level, status, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .params(t.id(), t.documentId(), t.patientId(), t.atc(), t.medication(), t.dosage(), t.level().name(), t.status(), author, Timestamp.from(at))
                .update();
    }

    public void updateStatus(UUID id, String status, String dosage) {
        jdbc.sql("UPDATE plan.treatment SET status = ?, dosage = ? WHERE id = ?")
                .params(status, dosage, id)
                .update();
    }

    public void insertDocument(UUID id, UUID treatmentId, String patientId, ChEmedDocuments.Type type, Confidentiality level,
                               String author, java.time.Instant at, String fhir) {
        jdbc.sql("INSERT INTO plan.document (id, treatment_id, patient_id, type, level, author, created_at, fhir) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb)")
                .params(id, treatmentId, patientId, type.name(), level.name(), author, Timestamp.from(at), fhir)
                .update();
    }

    public long documentCount(UUID treatmentId, ChEmedDocuments.Type type) {
        return jdbc.sql("SELECT count(*) FROM plan.document WHERE treatment_id = ? AND type = ?")
                .params(treatmentId, type.name())
                .query(Long.class)
                .single();
    }

    private static Treatment treatment(ResultSet rs, int n) throws SQLException {
        return new Treatment(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                Confidentiality.valueOf(rs.getString(7)), rs.getString(8));
    }
}
