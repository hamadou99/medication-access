package ch.unige.mssn.pmp.demo.steps;

import ch.unige.mssn.pmp.demo.PlanMedicationApplication;
import ch.unige.mssn.pmp.demo.support.IntegrationEnvironment;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@CucumberContextConfiguration
@SpringBootTest(classes = PlanMedicationApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        IntegrationEnvironment.register(registry);
    }
}
