package com.mifos.tests;

import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@Cucumber.Options(format = { "pretty", "html:target/LoanFloatingRates/cucumber-html-report",
		"json-pretty:target/LoanFloatingRates/cucumber-json-report.json" },
		features = { "src/test/resources/features/FloatingRates.feature" },
		glue = { "com.mifos.steps" })

public class FloatingRatesTest {

}
