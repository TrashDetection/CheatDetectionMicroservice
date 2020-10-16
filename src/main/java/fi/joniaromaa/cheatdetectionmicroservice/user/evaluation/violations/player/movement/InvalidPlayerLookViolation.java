package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;
import lombok.Getter;

public class InvalidPlayerLookViolation extends UserViolation
{
	@Getter private final float pitch;
	
	public InvalidPlayerLookViolation(float pitch)
	{
		super(ViolationType.CRITICAL);
		
		this.pitch = pitch;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client or using no head
	}
	
	@Override
	public String toString()
	{
		return String.format("Invalid Player Look | Pitch: %.8f", this.pitch);
	}
}
