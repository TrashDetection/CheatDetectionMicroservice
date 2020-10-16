package cucumberTests.user.evaluation.player.movement;

import javax.inject.Inject;

import cucumberTests.user.evaluation.CucumberUserEvaluation;
import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
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
		this.evaluation.readIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
	}
	
	@When("(player )stops sprinting")
	public void stopsSprinting() throws Exception
	{
		this.evaluation.readIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SPRINTING));
	}
}
