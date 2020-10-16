package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.action.StopBlockBreakTooSoonViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerDiggingIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;

public class BlockBreakModule extends GameStatusAwareSubmodule
{
	private boolean hasStartedTick;
	
	public BlockBreakModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.addIncomingHandlerPre(IPlayerDiggingIncomingPacket.class, this::digging);
		this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::tick);
	}

	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}

	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.hasStartedTick = false;
		}
	}
	
	private void digging(IPlayerDiggingIncomingPacket packet)
	{
		if (!this.getModule().isInGame())
		{
			return;
		}
		
		IPlayerDiggingIncomingPacket digging = (IPlayerDiggingIncomingPacket)packet;
		if (digging.getAction() == IPlayerDiggingIncomingPacket.Action.START_DESTROY_BLOCK)
		{
			this.hasStartedTick = true;
		}
		else if (digging.getAction() == IPlayerDiggingIncomingPacket.Action.STOP_DESTROY_BLOCK)
		{
			if (this.hasStartedTick)
			{
				this.addViolation(new StopBlockBreakTooSoonViolation());
			}
		}
	}
	
	private void tick(IPlayerIncomingPacket packet)
	{
		this.hasStartedTick = false;
	}
}
