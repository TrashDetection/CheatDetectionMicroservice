package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class PlayerBlockingInteractViolation extends UserViolation
{
	public PlayerBlockingInteractViolation()
	{
		super(ViolationType.WARN);
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		return "Player blocking interact";
	}
}
