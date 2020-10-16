package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.inspect;

import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class InspectInformationViolation extends UserViolation
{
	private final String info;
	
	public InspectInformationViolation(String info)
	{
		super(ViolationType.INFO);
		
		this.info = info;
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		return String.format("\tINFO | %s", this.info);
	}
}
