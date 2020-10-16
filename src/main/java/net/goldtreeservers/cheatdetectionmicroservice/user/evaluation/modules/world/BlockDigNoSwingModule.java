package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.world;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IAnimationIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerDiggingIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world.BlockDigNoSwingViolation;

public class BlockDigNoSwingModule extends Module
{
	private boolean waitingForSwing;
	
	public BlockDigNoSwingModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addIncomingHandlerPre(IPlayerDiggingIncomingPacket.class, this::handleDig);
		this.addIncomingHandlerPre(IAnimationIncomingPacket.class, this::handleAnimation);
	}

	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public boolean handlesOutgoingPackets()
	{
		return false;
	}

	@Override
	public boolean handlesIncomingPackets()
	{
		return true;
	}
	
	private void handleDig(IPlayerDiggingIncomingPacket packet)
	{
		IPlayerDiggingIncomingPacket digging = (IPlayerDiggingIncomingPacket)packet;
		if (digging.getAction() == IPlayerDiggingIncomingPacket.Action.START_DESTROY_BLOCK || digging.getAction() == IPlayerDiggingIncomingPacket.Action.STOP_DESTROY_BLOCK)
		{
			this.waitingForSwing = true;
		}
	}
	
	private void handleAnimation(IAnimationIncomingPacket packet)
	{
		this.waitingForSwing = false;
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (super.fireIncomingPacketHandler(packet))
		{
			return;
		}
		
		if (this.waitingForSwing)
		{
			this.addViolation(new BlockDigNoSwingViolation());
		}
		
		this.waitingForSwing = false;
	}
}
