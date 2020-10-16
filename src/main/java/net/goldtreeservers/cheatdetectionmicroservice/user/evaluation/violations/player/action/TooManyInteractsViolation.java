package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action;

import java.util.Arrays;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class TooManyInteractsViolation extends UserViolation
{
	private final int[] interacts;
	
	public TooManyInteractsViolation(int[] interacts)
	{
		super(ViolationType.CRITICAL);
		
		this.interacts = interacts;
	}

	@Override
	public int getViolationPoints()
	{
		if (this.interacts.length == 2)
		{
			return 3000; //Blatant, boo
		}
		else if (this.interacts.length == 3)
		{
			return 6000; //Woah!
		}
		else
		{
			return this.interacts.length * 3000;
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("Too many interacts | Changes: %s", Arrays.toString(this.interacts));
	}
}
