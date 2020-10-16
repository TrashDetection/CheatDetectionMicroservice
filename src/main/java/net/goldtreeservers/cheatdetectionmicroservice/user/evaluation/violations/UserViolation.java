package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UserViolation
{
	@Getter private final ViolationType type;
	
	public abstract int getViolationPoints();
}
