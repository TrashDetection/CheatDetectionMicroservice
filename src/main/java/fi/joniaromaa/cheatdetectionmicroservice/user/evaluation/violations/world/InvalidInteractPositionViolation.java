package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;
import fi.joniaromaa.minecrafthook.common.utils.IBlockPos;

public class InvalidInteractPositionViolation extends UserViolation
{
	private final IBlockPos location;
	
	public InvalidInteractPositionViolation(IBlockPos location)
	{
		super(ViolationType.CRITICAL);
		
		this.location = location;
	}

	@Override
	public int getViolationPoints()
	{
		return (int)Math.ceil(Math.random() * 33333); //Bruh moment, roll dice on them!
	}

	@Override
	public String toString()
	{
		return String.format("Invalid intreact position | Location: %s", this.location.toString());
	}
}
