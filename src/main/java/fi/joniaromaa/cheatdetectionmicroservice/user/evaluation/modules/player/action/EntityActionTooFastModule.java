package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import java.util.HashSet;
import java.util.Set;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.action.EntityActionTooFastViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;

public class EntityActionTooFastModule extends GameStatusAwareSubmodule
{
	private Set<String> actions;
	
	public EntityActionTooFastModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.actions = new HashSet<>(3);
		
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
			this.actions.clear();
		}
		else if (packet instanceof ICameraOutgoingPacket)
		{
			if (!this.getModule().isCurrentRenderingEntity())
			{
				this.actions.clear();
			}
		}
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IEntityActionIncomingPacket)
		{
			if (this.getModule().isCurrentRenderingEntity())
			{
				IEntityActionIncomingPacket actionPacket = (IEntityActionIncomingPacket)packet;
				if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.START_SNEAKING || actionPacket.getAction() == IEntityActionIncomingPacket.Action.STOP_SNEAKING)
				{
					if (!this.actions.add("sneak"))
					{
						this.addViolation(new EntityActionTooFastViolation("sneak"));
						
						return;
					}
				}
				else if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.START_SPRINTING || actionPacket.getAction() == IEntityActionIncomingPacket.Action.STOP_SPRINTING)
				{
					if (!this.actions.add("sprint"))
					{
						this.addViolation(new EntityActionTooFastViolation("sprint"));
						
						return;
					}
				}
				else if (actionPacket.getAction() == IEntityActionIncomingPacket.Action.RIDING_JUMP)
				{
					if (!this.actions.add("horseJump"))
					{
						this.addViolation(new EntityActionTooFastViolation("horse jump"));
						
						return;
					}
				}
			}
		}
		else if (packet instanceof IPlayerIncomingPacket)
		{
			if (this.getModule().isJustTeleported())
			{
				return;
			}
			
			this.actions.clear();
		}
	}
}
