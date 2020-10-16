package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.InvalidPlayerLookViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;

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
