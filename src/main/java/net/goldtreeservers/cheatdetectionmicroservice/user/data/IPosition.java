package net.goldtreeservers.cheatdetectionmicroservice.user.data;

public interface IPosition
{
	public double getX();
	public double getY();
	public double getZ();
	
	public default double length()
	{
        return Math.sqrt(IPosition.square(this.getX()) + IPosition.square(this.getY()) + IPosition.square(this.getZ()));
    }
	
	public default double dot(IPosition other)
	{
        return this.getX() * other.getX() + this.getY() * other.getY() + this.getZ() * other.getZ();
    }
	
	public default float angle(IPosition other)
	{
        double dot = this.dot(other) / (this.length() * other.length());

        return (float)Math.acos(dot);
    }
	
	public default Position immutable()
	{
		return new Position(this.getX(), this.getY(), this.getZ());
	}
	
	public static double square(double num)
	{
        return num * num;
    }
}
