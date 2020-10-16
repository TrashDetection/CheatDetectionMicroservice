package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class InvalidMovement extends UserViolation
{
	public InvalidMovement()
	{
		super(ViolationType.WARN_NO_LOG);
	}

	@Override
	public int getViolationPoints()
	{
		return 1;
	}
}
