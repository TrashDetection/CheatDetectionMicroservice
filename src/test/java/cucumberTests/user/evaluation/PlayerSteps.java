package cucumberTests.user.evaluation;

import javax.inject.Inject;

import cucumber.runtime.java.guice.ScenarioScoped;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IGamemode;
import io.cucumber.java.en.Given;

@ScenarioScoped
public class PlayerSteps
{
	private final CucumberUserEvaluation evaluation;
	
	private IGamemode.Type gamemodeType;
	
	@Inject
	public PlayerSteps(CucumberUserEvaluation evaluation)
	{
		this.evaluation = evaluation;
	}
	
	@Given("(has ){string} gamemode")
	public void gamemode(String gamemode) throws Exception
	{
		this.gamemodeType = IGamemode.Type.valueOf(gamemode.toUpperCase());
	}
	
	@Given("(player )spawns")
	public void spawns() throws Exception
	{
		this.evaluation.readConfirmedPacket(IJoinOutgoingPacket.newInstance(this.gamemodeType));
		
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.ground());
	}
	
	@Given("(player )stands")
	public void stands() throws Exception
	{
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.ground());
	}
	
	@Given("(player )moves to {double} {double} {double}")
	public void movesTo(double x, double y, double z) throws Exception
	{
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.ground(x, y, z));
	}
	
	@Given("(player )flies to {double} {double} {double}")
	public void flyTo(double x, double y, double z) throws Exception
	{
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.fly(x, y, z));
	}
}
