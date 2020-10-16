package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class TooManyMovePacketsViolation extends UserViolation
{
	private final int total;
	private final int expected;
	
	public TooManyMovePacketsViolation(int total, int expected)
	{
		super(ViolationType.CRITICAL);
		
		this.total = total;
		this.expected = expected;
	}

	@Override
	public int getViolationPoints()
	{
		int difference = this.total - this.expected;
		if (difference >= 0 && difference < 100) //Small, most likely client accidentally being dump
		{
			return 100 + difference;
		}
		else if (difference >= 100 && difference < 500) //Something sketchy going on
		{
			return difference * 15;
		}
		else if (difference >= 500 && difference < 1000) //They have done something way too often
		{
			return difference * 20;
		}
		else //And they are rolling it
		{
			return difference * 33;
		}
	}

	@Override
	public String toString()
	{
		return String.format("Too many move packets | Total: %d | Expected: %d | Dfiference: %d", this.total, this.expected, this.total - this.expected);
	}
}
