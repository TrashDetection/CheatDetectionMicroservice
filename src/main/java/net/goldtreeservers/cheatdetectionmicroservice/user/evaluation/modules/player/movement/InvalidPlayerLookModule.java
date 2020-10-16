package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.InvalidPlayerLookViolation;

public class InvalidPlayerLookModule extends GameStatusAwareSubmodule
{
	private static final float PITCH_MIN = -90.0F;
	private static final float PITCH_MAX = 90.0F;
	
	public InvalidPlayerLookModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IPlayerIncomingPacket)
		{
			if (this.getModule().isJustTeleported())
			{
				return;
			}
			
			IPlayerIncomingPacket playerPacket = (IPlayerIncomingPacket)packet;
			
			if (playerPacket.isRotating())
			{
				if (playerPacket.getPitch() < InvalidPlayerLookModule.PITCH_MIN || playerPacket.getPitch() > InvalidPlayerLookModule.PITCH_MAX)
				{
					this.addViolation(new InvalidPlayerLookViolation(playerPacket.getPitch()));
				}
			}
		}
	}
}
