package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.combat;

import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Location;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Position;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class ReachViolation extends UserViolation
{
	@Getter private final int entityId;
	
	@Getter private final Location playerLocation;
	@Getter private final Position targetPosition;
	
	@Getter private final double reach;
	@Getter private final double max;

	public ReachViolation(int entityId, Location playerLocation, Position targetPosition, double reach, double max)
	{
		super(ViolationType.CRITICAL);
		
		this.entityId = entityId;
		
		this.playerLocation = playerLocation;
		this.targetPosition = targetPosition;
		
		this.reach = reach;
		this.max = max;
	}

	@Override
	public int getViolationPoints()
	{
		double difference = this.reach - this.max;
		if (difference >= 0 && difference < 0.5) //People think this can't be detected, so give them as low points as possible
		{
			return 1 + (int)Math.ceil(difference * 25);
		}
		else if (difference >= 0.5 && difference < 1) //Blatant imo
		{
			return (int)Math.ceil(difference * 100);
		}
		else if (difference >= 1 && difference < 2) //No execuses for this
		{
			return (int)Math.ceil(difference * 500);
		}
		else //Over the top
		{
			return (int)Math.ceil(difference * 2500);
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("Reach violation | Entity Id: %d | Player location: %s | Target location: %s | Reach: %f", this.entityId, this.playerLocation, this.targetPosition, this.reach);
	}
}
