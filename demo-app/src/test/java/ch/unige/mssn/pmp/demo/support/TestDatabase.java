package ch.unige.mssn.pmp.demo.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Vrai PostgreSQL démarré une fois pour tous les tests, sans Docker, avec les deux rôles séparés du déploiement. */
public final class TestDatabase {

    public static final String OWNER = "pmp_owner";
    public static final String APP = "pmp_app";

    private static final EmbeddedPostgres POSTGRES = start();

    private TestDatabase() {
    }

    private static EmbeddedPostgres start() {
        try {
            EmbeddedPostgres postgres = EmbeddedPostgres.builder().start();
            try (Connection connection = postgres.getPostgresDatabase().getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("CREATE ROLE " + OWNER + " LOGIN PASSWORD 'test'");
                statement.execute("CREATE ROLE " + APP + " LOGIN PASSWORD 'test'");
                statement.execute("CREATE DATABASE pmp OWNER " + OWNER);
            }
            return postgres;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String jdbcUrl() {
        return POSTGRES.getJdbcUrl("postgres", "pmp").split("\\?")[0];
    }

    public static Connection connect(String user) throws SQLException {
        return POSTGRES.getDatabase(user, "pmp").getConnection();
    }

    /** Super-utilisateur du serveur : il figure l'administrateur qui contournerait les protections de la base. */
    public static Connection connectAsSuperuser() throws SQLException {
        return POSTGRES.getDatabase("postgres", "pmp").getConnection();
    }
}
