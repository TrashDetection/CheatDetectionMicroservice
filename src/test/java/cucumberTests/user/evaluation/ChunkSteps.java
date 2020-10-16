package cucumberTests.user.evaluation;

import javax.inject.Inject;

import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IChunkOutgoingPacket;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class ChunkSteps
{
	private final CucumberUserEvaluation evaluation;
	
	@Inject
	public ChunkSteps(CucumberUserEvaluation evaluation)
	{
		this.evaluation = evaluation;
	}
	
	@Given("(player )spawns inside chunk {int} {int}")
	public void spawnsInsideChunk(int x, int z) throws Exception
	{
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.ground());
		
		this.evaluation.readIncomingPacket(IPlayerIncomingPacket.ground(x << 4, 0, z << 4));
	}
	
	@When("(player )received chunk unload at {int} {int}")
	public void chunkUnload(int x, int z) throws Exception
	{
		this.evaluation.readConfirmedPacket(IChunkOutgoingPacket.chunkUnload(x, z));
	}
}
