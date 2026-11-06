package ch.unige.mssn.pmp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Tient lieu d'application du service : elle n'existe que pour démontrer le module, avec des données fictives. */
@SpringBootApplication
public class PlanMedicationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanMedicationApplication.class, args);
    }
}
