package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils.MathHelper;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.InvalidMovement;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IEntityVelocityOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IExplosionOutgoingPacket;

public class PlayerSpeedModule extends GameStatusAwareSubmodule
{
	private IPlayerIncomingPacket lastMove;
	
	private float rotationYaw;
	
	private double motionX;
	private double motionZ;
	
	private boolean onGround;

	private IPluginMessageIncomingPacket.DebugSPMoveFlying debugFlying;
	private IPluginMessageIncomingPacket.DebugSPMove debugMove;
	
	public PlayerSpeedModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.addRequireConfirmation(IEntityVelocityOutgoingPacket.class);
		this.addRequireConfirmation(IExplosionOutgoingPacket.class);
		
		this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::tick);
		this.addIncomingHandlerPre(IPluginMessageIncomingPacket.class, this::debug);
	}

	@Override
	public boolean handlesPluginMessages()
	{
		return true;
	}
	
	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IEntityVelocityOutgoingPacket)
		{
			IEntityVelocityOutgoingPacket velocityPacket = (IEntityVelocityOutgoingPacket)packet;
			if (this.getModule().getEntityId() == null || !this.getModule().getEntityId().equals(velocityPacket.getEntityId()))
			{
				return;
			}
			
			this.motionX = (double)velocityPacket.getVelocityX() / 8000.0D;
			this.motionZ = (double)velocityPacket.getVelocityZ() / 8000.0D;
		}
		else if (packet instanceof IExplosionOutgoingPacket)
		{
			IExplosionOutgoingPacket explosion = (IExplosionOutgoingPacket)packet;
			
			this.motionX += explosion.getVelocityX();
			this.motionZ += explosion.getVelocityZ();
		}
	}
	
	@SuppressWarnings("unused")
	private void tick(IPlayerIncomingPacket packet)
	{
		if (packet.isRotating())
		{
			//Don't count
			//this.rotationYaw = packet.getYaw();
		}
		
		if (this.getModule().isJustTeleported())
		{
			return;
		}
		
		if (this.lastMove == null)
		{
			if (packet.isMoving())
			{
				this.lastMove = packet;
				this.onGround = packet.isOnGround();
			}
			
			return;
		}
		
		if (!packet.isMoving() //We ain't moving, nothing to do
				|| this.getModule().getAttributes() == null //No idea about their move speed
				|| this.getModule().inVehicle() //They ain't moving by their self
				|| !Boolean.FALSE.equals(this.getModule().isSpectator()) //Can fly at varied speeds
				|| !this.getModule().isCurrentRenderingEntity() //If they aint current player
				|| !Boolean.FALSE.equals(this.getModule().getFlying()) //Bro ain't flying
			)
		{
			this.onGround = packet.isOnGround();
			
			return;
		}

		float moveForward = 1F; //Moving
		float moveStrafe = 1F; //Moving
		
		if (Boolean.TRUE.equals(this.getModule().getSneaking()))
		{
			moveStrafe *= 0.3D;
			moveForward *= 0.3D;
		}
		
		if (false) //Moving, not riding
		{
			moveStrafe *= 0.2F;
			moveForward *= 0.2F;
		}
		
		if (true) //Jumping
		{
			if (this.onGround)
			{
				if (!Boolean.FALSE.equals(this.getModule().isSprinting()))
				{
		            float f = this.rotationYaw * 0.017453292F;
		            
		            this.motionX -= (double)(MathHelper.sin(f) * 0.2F);
		            this.motionZ += (double)(MathHelper.cos(f) * 0.2F);
				}
			}
		}
		
		moveStrafe *= 0.98F;
		moveForward *= 0.98F;
		
		this.moveEntityWithHeading(moveStrafe, moveForward, packet);
		
		//LAST!
		this.lastMove = packet;
		this.onGround = packet.isOnGround();
	}

    @SuppressWarnings("unused")
	private void moveEntityWithHeading(float strafe, float forward, IPlayerIncomingPacket packet)
    {
		if(true) //Not in water or flying
		{
			if (true) //Not in lava or flying
			{
				final float friction;
				if (this.onGround)
				{
	                final float slipperiness = this.getStandingSlipperiness(this.onGround);
	                final float speedFactor = 0.16277136F / (slipperiness * slipperiness * slipperiness);
	                
					friction = this.getAIMoveSpeed() * speedFactor;
				}
				else
				{
					final float speedInAir = 0.02F;
					
					if (!Boolean.FALSE.equals(this.getModule().wasSprinting()))
					{
						friction = (float)((double)speedInAir + (double)speedInAir * 0.3D);
					}
					else
					{
						friction = speedInAir;
					}
				}
				
				this.moveFlying(strafe, forward, friction);
                
                if (false) //Onladder
                {
                    float f6 = 0.15F;
                    
                    this.motionX = MathHelper.clamp_double(this.motionX, (double)(-f6), (double)f6);
                    this.motionZ = MathHelper.clamp_double(this.motionZ, (double)(-f6), (double)f6);
                    
                    //Theres more stuff for motionY
                }

                this.moveEntity(this.motionX, this.motionZ, packet);
                
                if (false && false) //Collided horizontally, ladder
                {
                }
                
                //isRemote for Y stuff
				
                final float slipperiness = this.getStandingSlipperiness(this.onGround);
                
                this.motionX *= slipperiness;
                this.motionZ *= slipperiness;
			}
		}
    }
    
	private float getAIMoveSpeed()
	{
		return (float)this.getModule().getAttributes().getAttributeInstance(this.getEvaluation().getUser().getHook().getGameHook().getEntityHook().getAttributeHook().getSharedMovementSpeedAttribute()).getValue();
	}
	
	private float getStandingSlipperiness(boolean onGround)
	{
        if (onGround)
        {
        	return this.getBlockSlipperiness();
        }
        else
        {
        	return 0.91F;
        }
	}
	
	private float getBlockSlipperiness()
	{
		final float slipperiness = 0.6F; //Default solid block
		
		return slipperiness * 0.91F;
	}

	private void moveFlying(float strafe, float forward, float friction)
    {
		if (this.debugFlying != null && this.debugFlying.getForward() != 0 && this.debugFlying.getStrafe() != 0)
		{
			if (this.debugFlying.getStrafe() != strafe || this.debugFlying.getForward() != forward || this.debugFlying.getFriction() != friction)
			{
				//this.addViolation(new InspectInformationViolation(String.format("Wrong SP Flying | Strafe: %f | Forward: %f | Friction: %f | Expected Strafe: %f | Expected Forward: %f | Expected Friction: %f", strafe, forward, friction, this.debugFlying.getStrafe(), this.debugFlying.getForward(), this.debugFlying.getFriction())));
			}
		}
		
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F)
        {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F)
            {
                f = 1.0F;
            }

            f = friction / f;
            
            strafe = strafe * f;
            forward = forward * f;
            
            float f1 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
            float f2 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
            
            this.motionX += (double)(strafe * f2 - forward * f1);
            this.motionZ += (double)(forward * f2 + strafe * f1);
        }
    }
	
	private void moveEntity(double x, double z, IPlayerIncomingPacket packet)
	{
		if (this.debugMove != null)
		{
			if (this.debugMove.getX() != x || this.debugMove.getZ() != z)
			{
				//this.addViolation(new InspectInformationViolation(String.format("Wrong SP Move | X: %s | Z: %s | Expected X: %s | Expected Z: %s", x, z, this.debugMove.getX(), this.debugMove.getZ())));
			}
		}
		
		final double diffX = this.lastMove.getX() - packet.getX();
		final double diffZ = this.lastMove.getZ() - packet.getZ();
		
		final double threshold = 9.0E-4D + 0.03F; //Minecraft won't send movement thats below 9.0E-4D, 0.03F bcs this is buggy as hell still
	
		final double horizontalSpeed = Math.abs(Math.sqrt(diffX * diffX + diffZ * diffZ)) - threshold; //Only deal with positive numbers
		final double predictedSpeed = Math.sqrt(x * x + z * z);
		
		if (horizontalSpeed > predictedSpeed * 2)
		{
			this.addViolation(new InvalidMovement());
		}
	}
	
	private void debug(IPluginMessageIncomingPacket packet)
	{
		Object object = packet.getDataObject();
		if (object instanceof IPluginMessageIncomingPacket.DebugSPMove)
		{
			this.debugMove = (IPluginMessageIncomingPacket.DebugSPMove)object;
		}
		else if (object instanceof IPluginMessageIncomingPacket.DebugSPMoveFlying)
		{
			this.debugFlying = (IPluginMessageIncomingPacket.DebugSPMoveFlying)object;
		}
	}
}
