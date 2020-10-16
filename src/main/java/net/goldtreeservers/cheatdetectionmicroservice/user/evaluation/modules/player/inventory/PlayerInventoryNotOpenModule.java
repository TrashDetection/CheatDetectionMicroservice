package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.inventory;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IClickWindowIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IClientStatusIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.ICloseWindowIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.PlayerInventoryNotOpenViolation;

public class PlayerInventoryNotOpenModule extends Module
{
	private Boolean hasPlayerInventoryOpen;
	private Boolean allowWindowClickAfterWindowClose;
	
	public PlayerInventoryNotOpenModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addIncomingHandlerPre(IClientStatusIncomingPacket.class, this::handleClientStatus);
		this.addIncomingHandlerPre(ICloseWindowIncomingPacket.class, this::handleCloseWindow);
		this.addIncomingHandlerPre(IClickWindowIncomingPacket.class, this::handleClickWindow);
	}

	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() <= 319; //Fucking mojang ruined great check
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
	
	private void handleClientStatus(IClientStatusIncomingPacket packet)
	{
		if (packet.getStatus() == IClientStatusIncomingPacket.Status.OPEN_INVENTORY_ACHIEVEMENT)
		{
			this.hasPlayerInventoryOpen = true;
		}
	}
	
	private void handleCloseWindow(ICloseWindowIncomingPacket packet)
	{
		this.hasPlayerInventoryOpen = false;
		this.allowWindowClickAfterWindowClose = true;
	}
	
	private void handleClickWindow(IClickWindowIncomingPacket packet)
	{
		if (packet.getWindowId() == IClickWindowIncomingPacket.PLAYER_INVENTORY_ID)
		{
			if (Boolean.FALSE.equals(this.hasPlayerInventoryOpen))
			{
				if (Boolean.TRUE.equals(this.allowWindowClickAfterWindowClose))
				{
					//Slots can be moved with number keys, middle clicked and dropped even after closing the container, this can happen when example there is frame lag
					if (packet.getMode() == IClickWindowIncomingPacket.Mode.HOTBAR|| packet.getMode() ==  IClickWindowIncomingPacket.Mode.MIDDLE_CLICK || packet.getMode() ==  IClickWindowIncomingPacket.Mode.DROP)
					{
						return;
					}
				}

				this.addViolation(new PlayerInventoryNotOpenViolation(packet.getSlot(), packet.getButton(), packet.getActionNumber(), packet.getMode()));
			}
		}
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (super.fireIncomingPacketHandler(packet))
		{
			return;
		}
		
		this.allowWindowClickAfterWindowClose = false;
	}
}
