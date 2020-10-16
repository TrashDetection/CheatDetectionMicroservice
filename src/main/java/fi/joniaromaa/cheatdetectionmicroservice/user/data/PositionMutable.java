package fi.joniaromaa.cheatdetectionmicroservice.user.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PositionMutable implements IPosition
{
	@Getter @Setter private double x;
	@Getter @Setter private double y;
	@Getter @Setter private double z;
	
	public void setPosition(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString()
	{
		return String.format("PositionMutable(X, Y, Z)[%s %s %s]", this.x, this.y, this.z);
	}
}
