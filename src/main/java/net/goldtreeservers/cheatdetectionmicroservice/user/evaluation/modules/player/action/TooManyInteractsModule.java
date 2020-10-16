package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IUseEntityIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action.TooManyInteractsViolation;

public class TooManyInteractsModule extends GameStatusAwareSubmodule
{
	private IntSet interacts;
	
	public TooManyInteractsModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.interacts = new IntOpenHashSet(1);
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(ICameraOutgoingPacket.class);
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
			this.interacts.clear();
		}
		else if (packet instanceof ICameraOutgoingPacket)
		{
			if (!this.getModule().isCurrentRenderingEntity())
			{
				this.interacts.clear();
			}
		}
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IPlayerIncomingPacket)
		{
			int count = this.interacts.size();
			if (count > 0)
			{
				if (count > 1)
				{
					this.addViolation(new TooManyInteractsViolation(this.interacts.toIntArray()));
				}
				
				this.interacts.clear();
			}
		}
		else if (packet instanceof IUseEntityIncomingPacket)
		{
			if (this.getModule().isCurrentRenderingEntity())
			{
				IUseEntityIncomingPacket useEnityPacket = (IUseEntityIncomingPacket)packet;
				
				this.interacts.add(useEnityPacket.getEntityId());
			}
		}
	}
}
