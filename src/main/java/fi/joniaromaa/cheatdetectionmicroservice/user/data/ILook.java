package fi.joniaromaa.cheatdetectionmicroservice.user.data;

public interface ILook
{
	public float getYaw();
	public float getPitch();

	public default Position getDirection()
	{
		double yawRad = Math.toRadians(this.getYaw());
		double pitchRad = Math.toRadians(this.getPitch());
		
        double xz = Math.cos(pitchRad);
        
		return new Position(-xz * Math.sin(yawRad), -Math.sin(pitchRad), xz * Math.cos(yawRad));
	}
}
