package cucumberTests.user.evaluation.player.interaction;

import javax.inject.Inject;

import cucumberTests.user.evaluation.CucumberUserEvaluation;
import fi.joniaromaa.minecrafthook.common.game.item.IItemStack;
import fi.joniaromaa.minecrafthook.common.network.incoming.IBlockPlaceIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerDiggingIncomingPacket;
import io.cucumber.java.en.When;

public class PlayerInteractionSteps
{
	private final static int GOLDEN_SWORD = 283;
	private final static int GOLDEN_APPLE = 322;
	
	private final CucumberUserEvaluation evaluation;
	
	@Inject
	public PlayerInteractionSteps(CucumberUserEvaluation evaluation)
	{
		this.evaluation = evaluation;
	}
	
	@When("(player )starts blocking")
	public void startBlocking() throws Exception
	{
		this.evaluation.readIncomingPacket(IBlockPlaceIncomingPacket.interact(IItemStack.item(PlayerInteractionSteps.GOLDEN_SWORD)));
	}
	
	@When("(player )stops blocking")
	public void stopBlocking() throws Exception
	{
		this.evaluation.readIncomingPacket(IPlayerDiggingIncomingPacket.action(IPlayerDiggingIncomingPacket.Action.RELEASE_USE_ITEM));
	}
	
	@When("(player )eats {string}")
	public void eats(String value) throws Exception
	{
		this.evaluation.readIncomingPacket(IBlockPlaceIncomingPacket.interact(IItemStack.item(PlayerInteractionSteps.GOLDEN_APPLE)));
	}
}
