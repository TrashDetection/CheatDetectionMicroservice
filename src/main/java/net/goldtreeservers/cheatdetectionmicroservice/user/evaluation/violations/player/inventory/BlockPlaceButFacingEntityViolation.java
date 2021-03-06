package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class BlockPlaceButFacingEntityViolation extends UserViolation
{
	public BlockPlaceButFacingEntityViolation()
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
		return "Block place but facing entity";
	}
}
