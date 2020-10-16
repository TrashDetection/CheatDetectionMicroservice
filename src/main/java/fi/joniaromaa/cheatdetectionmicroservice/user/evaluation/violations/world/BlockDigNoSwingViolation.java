package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

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
