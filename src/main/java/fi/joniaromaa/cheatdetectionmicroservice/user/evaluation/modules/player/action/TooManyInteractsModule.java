package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.action.TooManyInteractsViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IUseEntityIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

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
