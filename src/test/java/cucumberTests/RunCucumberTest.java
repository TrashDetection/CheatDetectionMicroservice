package cucumberTests;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import net.goldtreeservers.cheatdetectionmicroservice.SetupMinecraft;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", features = "src/test/resources/cucumber", strict = true)
public class RunCucumberTest
{
	@BeforeClass
	public static void setupMinecraft()
	{
		SetupMinecraft.setup();
	}
}
