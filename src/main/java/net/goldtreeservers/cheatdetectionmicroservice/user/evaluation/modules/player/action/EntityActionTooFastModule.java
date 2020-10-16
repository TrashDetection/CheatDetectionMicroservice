package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.action;

import java.util.HashSet;
import java.util.Set;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action.EntityActionTooFastViolation;

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
		if (packet instanceof IIncomingEntityActionIncomingPacket)
		{
			if (this.getModule().isCurrentRenderingEntity())
			{
				IIncomingEntityActionIncomingPacket actionPacket = (IIncomingEntityActionIncomingPacket)packet;
				if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.START_SNEAKING || actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING)
				{
					if (!this.actions.add("sneak"))
					{
						this.addViolation(new EntityActionTooFastViolation("sneak"));
						
						return;
					}
				}
				else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.START_SPRINTING || actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING)
				{
					if (!this.actions.add("sprint"))
					{
						this.addViolation(new EntityActionTooFastViolation("sprint"));
						
						return;
					}
				}
				else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.RIDING_JUMP)
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
