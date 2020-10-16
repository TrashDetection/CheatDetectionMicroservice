package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerDiggingIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action.StopBlockBreakTooSoonViolation;

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
