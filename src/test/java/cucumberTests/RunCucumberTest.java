package cucumberTests;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import fi.joniaromaa.cheatdetectionmicroservice.SetupMinecraft;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@Ignore //TODO: BROKEN SINCE NEW CLASSLOADING
@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/cucumber", strict = true)
public class RunCucumberTest
{
	@BeforeClass
	public static void setupMinecraft() throws IOException
	{
		SetupMinecraft.setup();
	}
}
