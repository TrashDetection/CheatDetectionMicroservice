package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import java.util.Arrays;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class TooManySlotChangesViolation extends UserViolation
{
	private final short[] changes;
	
	public TooManySlotChangesViolation(short[] changes)
	{
		super(ViolationType.WARN);
		
		this.changes = changes;
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		return String.format("Too many slot changes | Changes: %s", Arrays.toString(this.changes));
	}
}
