package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.inventory;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IHeldItemChangeIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IHeldItemChangeOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.TooManySlotChangesViolation;

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
		if (packet instanceof IIncomingEntityActionIncomingPacket)
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
