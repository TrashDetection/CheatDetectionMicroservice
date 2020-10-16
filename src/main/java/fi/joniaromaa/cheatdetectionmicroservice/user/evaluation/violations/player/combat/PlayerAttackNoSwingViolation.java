package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.combat;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class PlayerAttackNoSwingViolation extends UserViolation
{
	private final int entityId;
	
	public PlayerAttackNoSwingViolation(int entityId)
	{
		super(ViolationType.CRITICAL);
		
		this.entityId = entityId;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Blatant one
	}
	
	@Override
	public String toString()
	{
		return String.format("Player attack no swing | Entity id: %d", this.entityId);
	}
}
