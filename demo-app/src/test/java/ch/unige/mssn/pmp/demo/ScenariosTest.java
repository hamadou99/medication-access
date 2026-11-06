package ch.unige.mssn.pmp.demo;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** Rejoue les scénarios écrits en langage courant (src/test/resources/scenarios). */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("scenarios")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "ch.unige.mssn.pmp.demo.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "json:target/scenarios/cucumber.json,pretty")
public class ScenariosTest {
}
