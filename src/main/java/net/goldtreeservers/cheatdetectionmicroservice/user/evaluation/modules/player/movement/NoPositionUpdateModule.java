package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityAttachOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPlayerPositionAndLookOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoPositionUpdateViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoTeleportConfirmViolation;

public class NoPositionUpdateModule extends GameStatusAwareSubmodule
{
	private int counter;
	
	private IPlayerPositionAndLookOutgoingPacket teleported;
	
	public NoPositionUpdateModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.addRequireConfirmation(IPlayerPositionAndLookOutgoingPacket.class);
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(IEntityAttachOutgoingPacket.class);
		
		this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::tick);
	}

	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IPlayerPositionAndLookOutgoingPacket)
		{
			this.teleported = (IPlayerPositionAndLookOutgoingPacket)packet;
		}
		else if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.counter = 0;
		}
		else if (packet instanceof IEntityAttachOutgoingPacket)
		{
			IEntityAttachOutgoingPacket attachPacket = (IEntityAttachOutgoingPacket)packet;
			
			//If its the player that is being attached to other entity
			if (!Boolean.FALSE.equals(this.getModule().isCurrentEntity(attachPacket.getEntityId())))
			{
				if (attachPacket.getVehicleId() != IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID)
				{
					this.counter = 0;
				}
			}
		}
	}

	@Override
	public void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		//TODO: This needs updated logic, check 2144_424_1600961885157 for details
		
		if (this.teleported != null && packet instanceof IPlayerIncomingPacket)
		{
			IPlayerIncomingPacket playerPacket = (IPlayerIncomingPacket)packet;
			if (playerPacket.isOnGround() || !playerPacket.isMoving() || !playerPacket.isRotating() || playerPacket.getX() != this.teleported.getX() || playerPacket.getY() != this.teleported.getY() || playerPacket.getZ() != this.teleported.getZ())
			{
				IPlayerPositionAndLookOutgoingPacket old = this.teleported;
				
				this.teleported = null;
				
				try
				{
					super.analyzeConfirmation(confirmed, packet);
				}
				finally
				{
					this.teleported = old;
				}
				
				return; //Skip, don't call twice!
			}
		}
		
		super.analyzeConfirmation(confirmed, packet);
	}

	private void tick(IPlayerIncomingPacket packet)
	{
		if (this.teleported == null)
		{
			if (this.getModule().inVehicle())
			{
				return;
			}
			
			if (packet.isMoving())
			{
				this.counter = 0;
			}
			else if (++this.counter == 21)
			{
				this.addViolation(new NoPositionUpdateViolation());
			}
		}
		else
		{
			if (packet.isOnGround() || !packet.isMoving() || !packet.isRotating() || packet.getX() != this.teleported.getX() || packet.getY() != this.teleported.getY() || packet.getZ() != this.teleported.getZ())
			{
				this.addViolation(new NoTeleportConfirmViolation()); //TODO: This needs to handle the teleport with relative coordinates
			}
			
			this.teleported = null;
		}
	}
}
