package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.inventory;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IConfirmTransactionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IHeldItemChangeIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IHeldItemChangeOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPlayerPositionAndLookOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.HeldItemNotChangedViolation;

public class HeldItemNotChangedModule extends GameStatusAwareSubmodule
{
	private Boolean state;
	
	private Byte from;
	private Byte to;
	
	private boolean teleported;
	
	public HeldItemNotChangedModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.addRequireConfirmation(IHeldItemChangeOutgoingPacket.class);
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(IPlayerPositionAndLookOutgoingPacket.class);
	}

	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() <= 47;
	}

	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IHeldItemChangeOutgoingPacket)
		{
			IHeldItemChangeOutgoingPacket heldPacket = (IHeldItemChangeOutgoingPacket)packet;
			
			if (this.state != null)
			{
				this.state = true;
			}

			this.to = heldPacket.getSlot();
		}
		else if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.from = null; //To make sure the state is not corrupted
		}
		else if (packet instanceof IPlayerPositionAndLookOutgoingPacket)
		{
			this.teleported = true;
		}
	}

	@Override
	public void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		if (confirmed instanceof IHeldItemChangeOutgoingPacket)
		{
			this.to = null;
			this.state = null;
		}
		
		super.analyzeConfirmation(confirmed, packet);
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IConfirmTransactionIncomingPacket)
		{
			return;
		}
		
		//The player packet mostly indicates that the tick was successfully done
		if (packet instanceof IPlayerIncomingPacket)
		{
			if (this.teleported)
			{
				this.teleported = false;
			}
			else
			{
				this.state = false;
			}
			
			return;
		}
		
		//The client will always send held item change packet first
		if (Boolean.TRUE.equals(this.state))
		{
			if (this.from != null && !this.to.equals(this.from) && !(packet instanceof IHeldItemChangeIncomingPacket))
			{
				this.addViolation(new HeldItemNotChangedViolation(this.from, this.to));
			}
		}
		
		this.state = null;
		
		if (packet instanceof IHeldItemChangeIncomingPacket)
		{
			IHeldItemChangeIncomingPacket heldItem = (IHeldItemChangeIncomingPacket)packet;
			
			this.from = (byte)heldItem.getSlot();
		}
	}
}
