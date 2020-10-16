package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.combat;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.combat.PlayerAttackNoSwingViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IAnimationIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IHeldItemChangeIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IUseEntityIncomingPacket;

public class PlayerAttackNoSwingModule extends GameStatusAwareSubmodule
{
	private boolean swungArm;
	
	public PlayerAttackNoSwingModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
	}
	
	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (!this.getModule().isInGame())
		{
			return;
		}
		
		if (packet instanceof IAnimationIncomingPacket)
		{
			this.swungArm = true;
		}
		else if (packet instanceof IUseEntityIncomingPacket)
		{
			IUseEntityIncomingPacket usePacket = (IUseEntityIncomingPacket)packet;
			if (usePacket.getAction() == IUseEntityIncomingPacket.Action.ATTACK)
			{
				if (!this.swungArm)
				{
					this.addViolation(new PlayerAttackNoSwingViolation(usePacket.getEntityId()));
				}
			}
			
			this.swungArm = false;
		}
		else if (!(packet instanceof IHeldItemChangeIncomingPacket)) //Held item change could be sent in the middle, so ignore that packet
		{
			this.swungArm = false;
		}
	}
}
