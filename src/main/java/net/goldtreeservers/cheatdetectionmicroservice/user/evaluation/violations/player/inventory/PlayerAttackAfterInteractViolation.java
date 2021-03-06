package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class PlayerAttackAfterInteractViolation extends UserViolation
{
	public PlayerAttackAfterInteractViolation()
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
		return "Player attack after interact";
	}
}
