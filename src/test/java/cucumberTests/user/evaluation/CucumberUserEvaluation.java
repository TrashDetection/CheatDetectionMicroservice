package cucumberTests.user.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import cucumber.runtime.java.guice.ScenarioScoped;
import fi.joniaromaa.cheatdetectionmicroservice.mocked.user.MockedUser;
import fi.joniaromaa.cheatdetectionmicroservice.mocked.user.evaluation.MockedUserEvaluation;
import fi.joniaromaa.minecrafthook.common.IMinecraftHook;
import fi.joniaromaa.minecrafthook.common.MinecraftHooks;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.Getter;

@ScenarioScoped
public class CucumberUserEvaluation
{
	@Getter private MockedUser user;
	@Getter private MockedUserEvaluation evaluation;

	@Given("(player )connected with version {string}")
	public void playerConnected(String version) throws Exception
	{
		IMinecraftHook hook = MinecraftHooks.getHook(version);
		if (hook == null)
		{
			throw new IllegalArgumentException("Could not find Minecraft hook with version: " + version);
		}
		
		this.user = new MockedUser(hook.getVersion().getProtocolVersion());
		
		this.evaluation = new MockedUserEvaluation(this.user);
		this.evaluation.setup();
	}
	
	@Then("(player )has no violations")
	public void hasNoViolation()
	{
		assertTrue(this.evaluation.getViolations().isEmpty());
	}
	
	@Then("(player )has following violations")
	public void hasViolations(List<String> violations)
	{
		assertEquals("Violation count wasn't expected; " + this.evaluation.getViolations(), violations.size(), this.evaluation.getViolations().size());
		assertTrue(this.evaluation.getViolations().stream().allMatch((v) -> violations.contains(v.toString())));
	}
	
	public void readIncomingPacket(IMinecraftIncomingPacket packet) throws Exception
	{
		this.user.getPackets().addIncomingPacket(packet);
		
		this.evaluation.read(this.user.getPackets());
	}
	
	public void readConfirmedPacket(IMinecraftOutgoingPacket packet) throws Exception
	{
		this.user.getPackets().addPacketConfirmedWithConfirmation(packet);

		this.evaluation.read(this.user.getPackets());
	}
	
	public void readConfirmPacket(IMinecraftOutgoingPacket packet) throws Exception
	{
		this.user.getPackets().addPacketConfirmed(packet);

		this.evaluation.read(this.user.getPackets());
	}
}
