package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.TooManySlotChangesViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IHeldItemChangeIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IHeldItemChangeOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class InvalidInventorySlotChangeModule extends GameStatusAwareSubmodule
{
	private ShortList changedSlots;
	
	private boolean ignoreNextPacket;
	
	public InvalidInventorySlotChangeModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.changedSlots = new ShortArrayList();
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(IHeldItemChangeOutgoingPacket.class);
	}

	@Override
	public boolean pre()
	{
		return false; //super.pre() && this.getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.changedSlots.clear();
		}
		else if (packet instanceof IHeldItemChangeOutgoingPacket)
		{
			this.ignoreNextPacket = true;
		}
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IEntityActionIncomingPacket)
		{
			int count = this.changedSlots.size();
			if (count > 0)
			{
				if (count > 1)
				{
					this.addViolation(new TooManySlotChangesViolation(this.changedSlots.toShortArray()));
				}
				
				this.changedSlots.clear();
			}
		}
		else if (packet instanceof IHeldItemChangeIncomingPacket)
		{
			if (!this.ignoreNextPacket)
			{
				IHeldItemChangeIncomingPacket heldChangePacket = (IHeldItemChangeIncomingPacket)packet;
				
				this.changedSlots.add(heldChangePacket.getSlot());
			}
			else
			{
				this.ignoreNextPacket = false;
			}
		}
	}
}
