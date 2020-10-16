package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.TooManyMovePacketsViolation;

public class TooManyMovePacketsModule extends GameStatusAwareSubmodule
{
	private int totalPackets;
	
	public TooManyMovePacketsModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
	}

	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IPlayerIncomingPacket)
		{
			if (this.getModule().isInGame() && !this.getModule().isJustTeleported())
			{
				this.totalPackets++;
			}
		}
	}

	@Override
	public void postAnalyzeIncoming()
	{
		int expectedMovementPackets = (int)Math.ceil(this.getEvaluation().getUser().getTimeAnalyzed() / (1000000D * 50D));

		if (this.totalPackets > expectedMovementPackets)
		{
			this.addViolation(new TooManyMovePacketsViolation(this.totalPackets, expectedMovementPackets));
		}
	}
}
