package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IBlockPos;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

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
