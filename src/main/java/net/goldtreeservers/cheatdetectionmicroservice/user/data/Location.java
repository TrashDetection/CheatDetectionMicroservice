package net.goldtreeservers.cheatdetectionmicroservice.user.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Location implements IPosition, ILook
{
	@Getter private final double x;
	@Getter private final double y;
	@Getter private final double z;
	
	@Getter private final float yaw;
	@Getter private final float pitch;

	@Override
	public String toString()
	{
		return String.format("Location(X, Y, Z, Yaw, Pitch)[%s %s %s %s %s]", this.x, this.y, this.z, this.yaw, this.pitch);
	}
}