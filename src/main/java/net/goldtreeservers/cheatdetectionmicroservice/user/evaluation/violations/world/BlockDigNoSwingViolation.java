package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class BlockDigNoSwingViolation extends UserViolation
{
	public BlockDigNoSwingViolation()
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
		return "Block dig no swing";
	}
}
