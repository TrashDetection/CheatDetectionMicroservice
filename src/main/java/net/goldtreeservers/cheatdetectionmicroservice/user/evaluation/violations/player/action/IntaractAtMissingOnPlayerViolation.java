package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class IntaractAtMissingOnPlayerViolation extends UserViolation
{
	private final int entityId;
	
	public IntaractAtMissingOnPlayerViolation(int entityId)
	{
		super(ViolationType.CRITICAL);
		
		this.entityId = entityId;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}

	@Override
	public String toString()
	{
		return String.format("Interact at missing on player | Entity id: %d", this.entityId);
	}
}
