package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class InvalidInteractFacingViolation extends UserViolation
{
	private final float facingX;
	private final float facingY;
	private final float facingZ;
	
	public InvalidInteractFacingViolation(float facingX, float facingY, float facingZ)
	{
		super(ViolationType.CRITICAL);
		
		this.facingX = facingX;
		this.facingY = facingY;
		this.facingZ = facingZ;
	}

	@Override
	public int getViolationPoints()
	{
		return (int)Math.ceil(Math.random() * 33333); //Bruh moment, roll dice on them!
	}

	@Override
	public String toString()
	{
		return String.format("Invalid interact facing | X: %f | Y: %f | Z: %f", this.facingX, this.facingY, this.facingZ);
	}
}
