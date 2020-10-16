package cucumberTests.user.evaluation.player.movement;

import javax.inject.Inject;

import cucumberTests.user.evaluation.CucumberUserEvaluation;
import fi.joniaromaa.minecrafthook.common.network.incoming.IConfirmTransactionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IPlayerPositionAndLookOutgoingPacket;
import io.cucumber.java.en.When;

public class PlayerTeleportSteps
{
	private final CucumberUserEvaluation evaluation;
	
	@Inject
	public PlayerTeleportSteps(CucumberUserEvaluation evaluation)
	{
		this.evaluation = evaluation;
	}
	
	@When("(player )is being teleported to {double} {double} {double}")
	public void teleportTo(double x, double y, double z) throws Exception
	{
		this.evaluation.readConfirmPacket(IPlayerPositionAndLookOutgoingPacket.newInstance(x, y, z));
	}
	
	@When("(player )sends teleport pre confirm")
	public void preConfirm() throws Exception
	{
		this.evaluation.readIncomingPacket(IConfirmTransactionIncomingPacket.newPreConfirmInstance());
	}
	
	@When("(player )sends teleport confirm")
	public void confirm() throws Exception
	{
		this.evaluation.readIncomingPacket(IConfirmTransactionIncomingPacket.newConfirmInstance());
	}
}
