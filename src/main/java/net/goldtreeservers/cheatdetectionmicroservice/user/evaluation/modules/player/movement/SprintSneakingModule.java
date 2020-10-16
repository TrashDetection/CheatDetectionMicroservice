package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoSneakToggleViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SneakStatusUpdateViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintSneakingViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintStatusUpdateViolation;

public class SprintSneakingModule extends GameStatusAwareSubmodule
{
	private boolean sneakOffNext;

	private Boolean sneaking;
	private Boolean sprinting;

	public SprintSneakingModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
	}
	
	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.sneakOffNext = false;
			
			this.sneaking = false;
			this.sprinting = false;
		}
	}

	@Override
	public void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		//These reset the state, so if this happens in middle of confirmation we can have corrupt state
		//Ship these packets to make sure we don't have false positive
		if (confirmed instanceof IJoinOutgoingPacket || confirmed instanceof IRespawnOutgoingPacket)
		{
			if (packet instanceof IIncomingEntityActionIncomingPacket)
			{
				return;
			}
		}
		
		this.analyzeIncoming(packet);
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IIncomingEntityActionIncomingPacket)
		{
			IIncomingEntityActionIncomingPacket actionPacket = (IIncomingEntityActionIncomingPacket)packet;
			if (this.sneakOffNext && actionPacket.getAction() != IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING)
			{
				this.addViolation(new NoSneakToggleViolation());
				
				this.sneakOffNext = false;
			}
			
			if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.START_SNEAKING)
			{
				if (Boolean.TRUE.equals(this.sneaking))
				{
					this.addViolation(new SneakStatusUpdateViolation(true));
				}
				
				this.sneaking = true;
				
				if (Boolean.TRUE.equals(this.sprinting))
				{
					this.addViolation(new SprintSneakingViolation());
				}
			}
			else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING)
			{
				if (Boolean.FALSE.equals(this.sneaking))
				{
					this.addViolation(new SneakStatusUpdateViolation(false));
				}
				
				this.sneaking = false;
				this.sneakOffNext = false;
			}
			else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.START_SPRINTING)
			{
				if (Boolean.TRUE.equals(this.sprinting))
				{
					this.addViolation(new SprintStatusUpdateViolation(true));
				}
				
				this.sprinting = true;
				this.sneakOffNext = Boolean.TRUE.equals(this.sneaking);
				
				return;
			}
			else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING)
			{
				if (Boolean.FALSE.equals(this.sprinting))
				{
					this.addViolation(new SprintStatusUpdateViolation(false));
				}
				
				this.sprinting = false;
			}
		}
		else if (packet instanceof IPlayerIncomingPacket)
		{
			if (Boolean.TRUE.equals(this.sneaking) && Boolean.TRUE.equals(this.sprinting))
			{
				this.addViolation(new SprintSneakingViolation());
			}
		}
		
		if (this.sneakOffNext)
		{
			this.addViolation(new NoSneakToggleViolation());
			
			this.sneakOffNext = false;
		}
		
		return;
	}
}
