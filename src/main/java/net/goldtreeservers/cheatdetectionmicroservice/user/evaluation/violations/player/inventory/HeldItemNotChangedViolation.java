package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class HeldItemNotChangedViolation extends UserViolation
{
	private final byte from;
	private final byte to;
	
	public HeldItemNotChangedViolation(byte from, byte to)
	{
		super(ViolationType.WARN);
		
		this.from = from;
		this.to = to;
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}

	@Override
	public String toString()
	{
		return String.format("Held Item Not Changed | From: %d | To: %d", this.from, this.to);
	}
}
