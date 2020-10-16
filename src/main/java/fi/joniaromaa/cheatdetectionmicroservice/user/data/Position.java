package fi.joniaromaa.cheatdetectionmicroservice.user.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Position implements IPosition
{
	@Getter private final double x;
	@Getter private final double y;
	@Getter private final double z;
	
	public Position sub(Position other)
	{
		return new Position(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
	}
	
	public Position sub(Location other)
	{
		return new Position(this.x - other.getX(), this.y - other.getY(), this.z - other.getZ());
	}
	
	@Override
	public String toString()
	{
		return String.format("Position(X, Y, Z)[%s %s %s]", this.x, this.y, this.z);
	}
}
