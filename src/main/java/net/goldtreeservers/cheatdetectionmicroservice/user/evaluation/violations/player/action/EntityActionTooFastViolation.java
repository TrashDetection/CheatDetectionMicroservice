package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class EntityActionTooFastViolation extends UserViolation
{
	private final String type;
	
	public EntityActionTooFastViolation(String type)
	{
		super(ViolationType.CRITICAL);
		
		this.type = type;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}
	
	@Override
	public String toString()
	{
		return String.format("Entity action too fast violation | Action: %s", this.type);
	}
}
