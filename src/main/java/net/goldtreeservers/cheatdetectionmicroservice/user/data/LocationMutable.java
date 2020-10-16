package net.goldtreeservers.cheatdetectionmicroservice.user.data;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LocationMutable
{
	private double x;
	private double y;
	private double z;
	
	private float yaw;
	private float pitch;
	
	public void deserialize(ByteBuf out)
	{
		out.writeDouble(this.x);
		out.writeDouble(this.y);
		out.writeDouble(this.z);
		
		out.writeFloat(this.yaw);
		out.writeFloat(this.pitch);
	}
	
	public void copyFrom(LocationMutable other)
	{
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
		
		this.yaw = other.yaw;
		this.pitch = other.pitch;
	}
	
	public LocationMutable copy()
	{
		return new LocationMutable(this.x, this.y, this.z, this.yaw, this.pitch);
	}

	public Location immutable()
	{
		return new Location(this.x, this.y, this.z, this.yaw, this.pitch);
	}
	
	@Override
	public String toString()
	{
		return String.format("LocationMutable(X, Y, Z, Yaw, Pitch)[%s %s %s %s %s]", this.x, this.y, this.z, this.yaw, this.pitch);
	}
}