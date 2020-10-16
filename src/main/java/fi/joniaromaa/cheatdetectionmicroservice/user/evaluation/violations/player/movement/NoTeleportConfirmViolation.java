package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class NoTeleportConfirmViolation extends UserViolation
{
	public NoTeleportConfirmViolation()
	{
		super(ViolationType.WARN_NO_LOG);
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}
	
	@Override
	public String toString()
	{
		return "No Teleport Confirm";
	}
}
