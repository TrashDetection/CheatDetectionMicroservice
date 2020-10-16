package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IKeepAliveIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.UserNoUpdateViolation;

public class UserNoUpdateModule extends Module
{
	private int keepAlived;
	
	public UserNoUpdateModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addIncomingHandlerPre(IKeepAliveIncomingPacket.class, this::handleKeepAlive);
		this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::handlePlayer);
	}

	@Override
	public boolean pre()
	{
		return false; //super.pre() && this.getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public boolean handlesAsyncPackets()
	{
		return true;
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
	
	private void handleKeepAlive(IKeepAliveIncomingPacket packet)
	{
		this.keepAlived++;
		
		if (this.keepAlived > 1)
		{
			this.addViolation(new UserNoUpdateViolation(this.keepAlived));
		}
	}
	
	private void handlePlayer(IPlayerIncomingPacket packet)
	{
		this.keepAlived = 0;
	}
}
