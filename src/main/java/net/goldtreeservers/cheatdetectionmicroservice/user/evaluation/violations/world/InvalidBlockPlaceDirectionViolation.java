package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class InvalidBlockPlaceDirectionViolation extends UserViolation
{
	private final int direction;
	
	public InvalidBlockPlaceDirectionViolation(int direction)
	{
		super(ViolationType.CRITICAL);
		
		this.direction = direction;
	}

	@Override
	public int getViolationPoints()
	{
		return (int)Math.ceil(Math.random() * 33333); //Bruh moment, roll dice on them!
	}
	
	@Override
	public String toString()
	{
		return String.format("Invalid block place direction | Direction: %d", this.direction);
	}
}
