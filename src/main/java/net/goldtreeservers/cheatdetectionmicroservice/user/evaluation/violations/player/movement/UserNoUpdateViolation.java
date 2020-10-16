package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class UserNoUpdateViolation extends UserViolation
{
	@Getter private final int updates;
	
	public UserNoUpdateViolation(int updates)
	{
		super(ViolationType.WARN);
		
		this.updates = updates;
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		return String.format("User No Update | Updates: %d", this.updates);
	}
}
