package cucumberTests.user.evaluation.player.movement;

import javax.inject.Inject;

import cucumberTests.user.evaluation.CucumberUserEvaluation;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import io.cucumber.java.en.When;

public class PlayerActionSteps
{
	private final CucumberUserEvaluation evaluation;
	
	@Inject
	public PlayerActionSteps(CucumberUserEvaluation evaluation)
	{
		this.evaluation = evaluation;
	}
	
	@When("(player )starts sprinting")
	public void startsSprinting() throws Exception
	{
		this.evaluation.readIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
	}
	
	@When("(player )stops sprinting")
	public void stopsSprinting() throws Exception
	{
		this.evaluation.readIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING));
	}
}
