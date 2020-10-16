package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;

public abstract class GameStatusAwareSubmodule extends Module
{
	@Getter private final GameStatusAwareModule module;
	
	public GameStatusAwareSubmodule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.module = module;
	}
	
	protected void addRequireConfirmation(Class<? extends IMinecraftOutgoingPacket> clazz)
	{
		this.getModule().addConfirmPacket(clazz);
	}
	
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		//NOP
	}

	public void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		this.analyzeIncoming(packet);
	}

	public void playerRemovedFromChunk()
	{
		//NOP
	}

	@Override
	public boolean handlesOutgoingPackets()
	{
		return true;
	}

	@Override
	public boolean handlesIncomingPackets()
	{
		return true;
	}
	
}
