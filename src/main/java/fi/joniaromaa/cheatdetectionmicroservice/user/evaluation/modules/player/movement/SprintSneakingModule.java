package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoSneakToggleViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SneakStatusUpdateViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintSneakingViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintStatusUpdateViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;

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
			if (packet instanceof IEntityActionIncomingPacket)
			{
				return;
			}
		}
		
		this.analyzeIncoming(packet);
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IEntityActionIncomingPacket)
		{
			IEntityActionIncomingPacket actionPacket = (IEntityActionIncomingPacket)packet;
			if (this.sneakOffNext && actionPacket.getAction() != IEntityActionIncomingPacket.Action.STOP_SNEAKING)
			{
				this.addViolation(new NoSneakToggleViolation());
				
				this.sneakOffNext = false;
			}
			
			if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.START_SNEAKING)
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
			else if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.STOP_SNEAKING)
			{
				if (Boolean.FALSE.equals(this.sneaking))
				{
					this.addViolation(new SneakStatusUpdateViolation(false));
				}
				
				this.sneaking = false;
				this.sneakOffNext = false;
			}
			else if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.START_SPRINTING)
			{
				if (Boolean.TRUE.equals(this.sprinting))
				{
					this.addViolation(new SprintStatusUpdateViolation(true));
				}
				
				this.sprinting = true;
				this.sneakOffNext = Boolean.TRUE.equals(this.sneaking);
				
				return;
			}
			else if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.STOP_SPRINTING)
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
