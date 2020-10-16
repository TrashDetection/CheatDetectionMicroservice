package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class SprintStatusUpdateViolation extends UserViolation
{
	private final boolean sprinting;
	
	public SprintStatusUpdateViolation(boolean sprinting)
	{
		super(ViolationType.CRITICAL);
		
		this.sprinting = sprinting;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof SprintStatusUpdateViolation)
		{
			return this.sprinting == ((SprintStatusUpdateViolation)other).sprinting;
		}
		
		return false;
	}

	@Override
	public String toString()
	{
		return String.format("Sprint status update violation | Sprinting: %s", this.sprinting);
	}
}