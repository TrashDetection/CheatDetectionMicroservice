package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

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
