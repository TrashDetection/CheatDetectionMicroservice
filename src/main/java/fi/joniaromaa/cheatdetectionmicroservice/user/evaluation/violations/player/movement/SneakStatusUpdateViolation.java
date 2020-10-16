package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class SneakStatusUpdateViolation extends UserViolation
{
	private final boolean sneaking;
	
	public SneakStatusUpdateViolation(boolean sneaking)
	{
		super(ViolationType.CRITICAL);
		
		this.sneaking = sneaking;
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
		
		if (other instanceof SneakStatusUpdateViolation)
		{
			return this.sneaking == ((SneakStatusUpdateViolation)other).sneaking;
		}
		
		return false;
	}

	@Override
	public String toString()
	{
		return String.format("Sneak status update violation | Sneaking: %s", this.sneaking);
	}
}
