package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class OnBlockingKeptSprintViolation extends UserViolation
{
	public OnBlockingKeptSprintViolation()
	{
		super(ViolationType.CRITICAL);
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}

	@Override
	public String toString()
	{
		return "On blocking kept sprint";
	}
}
