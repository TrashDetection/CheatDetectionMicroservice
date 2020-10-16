package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.action;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class StopBlockBreakTooSoonViolation extends UserViolation
{
	public StopBlockBreakTooSoonViolation()
	{
		super(ViolationType.CRITICAL);
	}

	@Override
	public int getViolationPoints()
	{
		return 1;
	}

	@Override
	public String toString()
	{
		return "Stop block break too soon";
	}
}
