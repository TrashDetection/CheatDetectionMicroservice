package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.InvalidContainerTransactionIdViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IClickWindowIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.ICloseWindowIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.ICloseWindowOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IOpenWindowOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;

public class InvalidContainerTransactionIdModule extends GameStatusAwareSubmodule
{
	private Int2ShortMap nextTransactionId;

	private boolean inventoryOpen;
	private int openInventory;
	
	public InvalidContainerTransactionIdModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.nextTransactionId = new Int2ShortOpenHashMap();
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(IOpenWindowOutgoingPacket.class);
		this.addRequireConfirmation(ICloseWindowOutgoingPacket.class);
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
			this.inventoryOpen = true;
			this.openInventory = 0;
			
			this.nextTransactionId.remove(0); //Player inventory
		}
		else if (packet instanceof IOpenWindowOutgoingPacket)
		{
			IOpenWindowOutgoingPacket windowPacket = (IOpenWindowOutgoingPacket)packet;

			this.inventoryOpen = true;
			this.openInventory = windowPacket.getWindowId();
			
			this.nextTransactionId.remove(windowPacket.getWindowId());
		}
		else if (packet instanceof ICloseWindowOutgoingPacket)
		{
			this.inventoryOpen = true;
			this.openInventory = 0;
		}
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IClickWindowIncomingPacket)
		{
			if (this.inventoryOpen)
			{
				IClickWindowIncomingPacket clickWindowPacket = (IClickWindowIncomingPacket)packet;
				
				boolean doCheck = this.nextTransactionId.containsKey(this.openInventory);
				if (doCheck)
				{
					short oldValue = this.nextTransactionId.get(this.openInventory);
					if ((oldValue + 1) != clickWindowPacket.getActionNumber())
					{
						//ONLY if our main inventory isn't seen as "open"
						//Is it the player inventory? Open inventory can be forced to close without sending a close packet
						if (this.openInventory != IClickWindowIncomingPacket.PLAYER_INVENTORY_ID && clickWindowPacket.getWindowId() == IClickWindowIncomingPacket.PLAYER_INVENTORY_ID)
						{
							if (!this.nextTransactionId.containsKey(IClickWindowIncomingPacket.PLAYER_INVENTORY_ID))
							{
								return;
							}
							
							oldValue = this.nextTransactionId.put(IClickWindowIncomingPacket.PLAYER_INVENTORY_ID, clickWindowPacket.getActionNumber());
							if ((oldValue + 1) == clickWindowPacket.getActionNumber())
							{
								return;
							}
						}
						
						this.addViolation(new InvalidContainerTransactionIdViolation(clickWindowPacket.getWindowId(), clickWindowPacket.getSlot(), clickWindowPacket.getButton(), oldValue, clickWindowPacket.getActionNumber(), clickWindowPacket.getMode(), this.openInventory));
					
						return;
					}
				}
				
				this.nextTransactionId.put(this.openInventory, clickWindowPacket.getActionNumber());
			}
		}
		else if (packet instanceof ICloseWindowIncomingPacket)
		{
			this.inventoryOpen = true;
			this.openInventory = 0;
		}
	}
}
