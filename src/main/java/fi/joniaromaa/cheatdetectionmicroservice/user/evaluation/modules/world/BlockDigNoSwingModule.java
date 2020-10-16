package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.Module;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world.BlockDigNoSwingViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IAnimationIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerDiggingIncomingPacket;

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
