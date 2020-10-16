package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class SprintSneakingViolation extends UserViolation
{
	public SprintSneakingViolation()
	{
		super(ViolationType.CRITICAL);
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		return other instanceof SprintSneakingViolation;
	}

	@Override
	public String toString()
	{
		return "Sprint sneaking";
	}
}
