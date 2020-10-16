package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.combat;

import net.goldtreeservers.cheatdetectionmicroservice.user.data.Location;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Position;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class NotFacingAttackingEntityViolation extends UserViolation
{
	private final int entityId;
	
	private final Location playerLocation;
	private final Position targetPosition;

	public NotFacingAttackingEntityViolation(int entityId, Location playerLocation, Position targetPosition)
	{
		super(ViolationType.CRITICAL);
		
		this.entityId = entityId;
		
		this.playerLocation = playerLocation;
		this.targetPosition = targetPosition;
	}

	@Override
	public int getViolationPoints()
	{
		float angle = Math.abs(this.getAngle());
		if (angle >= 0 && angle < 20) //Almost unnoticeable
		{
			return 100 + (int)Math.ceil(angle);
		}
		else if (angle >= 20 && angle < 45) //Starting to get blatant
		{
			return (int)Math.ceil(angle * 3);
		}
		else if (angle >= 45 && angle < 90) //Blatant
		{
			return (int)Math.ceil(angle * 5);
		}
		else //Don't even try
		{
			return (int)Math.ceil(angle * 10);
		}
	}

	public float getAngle()
	{
		Position targetDirection = this.targetPosition.sub(this.playerLocation);
		Position playerDirection = this.playerLocation.getDirection();
		
		return (float)Math.toDegrees(targetDirection.angle(playerDirection));
	}
	
	@Override
	public String toString()
	{
		return String.format("Not facing attacking entity | Entity Id: %d | Player location: %s | Target location: %s | Angle: %f", this.entityId, this.playerLocation, this.targetPosition, this.getAngle());
	}
}
