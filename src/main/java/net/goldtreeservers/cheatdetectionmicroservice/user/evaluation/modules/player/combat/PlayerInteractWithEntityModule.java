package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.combat;

import java.util.ArrayDeque;
import java.util.Queue;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IUseEntityIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IAnimationOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IDestroyEntitiesOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityAttachOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityTeleportOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPlayerPositionAndLookOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.ISpawnPlayerOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IUseBedOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IBlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.cheatdetectionmicroservice.core.CheatDetectionMicroservice;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.IPosition;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.LocationMutable;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Position;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.PositionMutable;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.player.combat.debug.PlayerInteractWithEntityModuleDebug;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.action.IntaractAtMissingOnPlayerViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.combat.NotFacingAttackingEntityViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.combat.ReachViolation;

public class PlayerInteractWithEntityModule extends GameStatusAwareSubmodule
{
	public static final boolean DEBUG = PlayerInteractWithEntityModule.getEnableDebug();
	
	private static final boolean COLLISION_BOX_LENIENT = PlayerInteractWithEntityModule.getLenientCollisionBox(); //Allows hitting from further away
	
	//Current incoming state
	@Getter(AccessLevel.PROTECTED) private LocationMutable currentLocation;
	@Getter(AccessLevel.PROTECTED) private LocationMutable lastLocation;
	
	private Queue<HitData> hits;

	//Outgoing states
	private Int2ObjectMap<Entity> entities;
	
	private boolean skipNextTick;
	
	private boolean hadInteractAt;
	
	private Boolean sneakingPending;
	private boolean sneaking;
	
	public PlayerInteractWithEntityModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.hits = new ArrayDeque<>(2);
		
		this.entities = new Int2ObjectOpenHashMap<>();
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(ISpawnPlayerOutgoingPacket.class);
		this.addRequireConfirmation(IEntityOutgoingPacket.class);
		this.addRequireConfirmation(IEntityTeleportOutgoingPacket.class);
		this.addRequireConfirmation(IEntityAttachOutgoingPacket.class);
		this.addRequireConfirmation(ICameraOutgoingPacket.class);
		this.addRequireConfirmation(IPlayerPositionAndLookOutgoingPacket.class);
		this.addRequireConfirmation(IUseBedOutgoingPacket.class);
		this.addRequireConfirmation(IDestroyEntitiesOutgoingPacket.class);
	}
	
	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IPlayerIncomingPacket)
		{
			//If there is player packet in the middle of the confirmation it could mean the entity position can be anywhere
			if (confirmed instanceof IEntityOutgoingPacket)
			{
				IEntityOutgoingPacket entityPacket = (IEntityOutgoingPacket)confirmed;
				
				Entity entity = this.entities.get(entityPacket.getEntityId());
				if (entity != null)
				{
					entity.unreliableLocationSet();
				}
			}
			else if (confirmed instanceof IEntityTeleportOutgoingPacket)
			{
				IEntityTeleportOutgoingPacket teleportPacket = (IEntityTeleportOutgoingPacket)confirmed;

				Entity entity = this.entities.get(teleportPacket.getEntityId());
				if (entity != null)
				{
					entity.unreliableLocationSet();
				}
			}
		}
		
		super.analyzeConfirmation(confirmed, packet);
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (!(packet instanceof IUseEntityIncomingPacket))
		{
			this.hadInteractAt = false;
		}
		
		if (packet instanceof IPlayerIncomingPacket)
		{
			IPlayerIncomingPacket playerPacket = (IPlayerIncomingPacket)packet;
			
			//If the user is in vehicle, don't update its position
			if (!this.getModule().inVehicle())
			{
				if (this.currentLocation != null || (playerPacket.isMoving() && playerPacket.isRotating() && (!this.skipNextTick || this.currentLocation != null)))
				{
					if (this.lastLocation != null)
					{
						this.lastLocation.copyFrom(this.currentLocation);
					}
					else if (this.currentLocation != null)
					{
						this.lastLocation = this.currentLocation.copy();
					}
					else
					{
						this.currentLocation = new LocationMutable(-1.69D, -1.69D, -1.69D, -1.69F, -1.69F);
					}
					
					if (playerPacket.isMoving())
					{
						this.currentLocation.setX(playerPacket.getX());
						this.currentLocation.setY(playerPacket.getY());
						this.currentLocation.setZ(playerPacket.getZ());
					}
					
					if (!this.skipNextTick && playerPacket.isRotating())
					{
						this.currentLocation.setYaw(playerPacket.getYaw());
						this.currentLocation.setPitch(playerPacket.getPitch());
					}
				}
			}
			
			if (this.skipNextTick)
			{
				this.skipNextTick = false;
				
				return;
			}
			
			this.tick();
			
			if (this.sneakingPending != null)
			{
				this.sneaking = this.sneakingPending;
				
				this.sneakingPending = null;
			}
			
			return;
		}
		else if (packet instanceof IIncomingEntityActionIncomingPacket)
		{
			IIncomingEntityActionIncomingPacket actionPacket = (IIncomingEntityActionIncomingPacket)packet;
			if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.START_SNEAKING)
			{
				this.sneakingPending = true;
			}
			else if (actionPacket.getAction() == IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING)
			{
				this.sneakingPending = false;
			}
		}
		
		//If the user is in vehicle the position is unstable
		//If the user is looking at the world at other entity eyes, we aint getting location updates
		if (this.getModule().inVehicle() || !this.getModule().isCurrentRenderingEntity())
		{
			return;
		}

		//Okay all good, now if the current position is stable we can check for hits
		if (this.currentLocation != null && this.lastLocation != null)
		{
			if (packet instanceof IUseEntityIncomingPacket)
			{
				IUseEntityIncomingPacket usePacket = (IUseEntityIncomingPacket)packet;
				
				Entity entity = this.entities.get(usePacket.getEntityId());
				if (entity != null) //Is player
				{
					if (entity.isReliableLocation())
					{
						this.hits.add(this.createHitData(entity, usePacket));
					}
					
					if (usePacket.getAction() == IUseEntityIncomingPacket.Action.INTERACT_AT)
					{
						this.hadInteractAt = true;
						
						return;
					}
					
					if (usePacket.getAction() == IUseEntityIncomingPacket.Action.INTERACT)
					{
						if (!this.hadInteractAt)
						{
							this.addViolation(new IntaractAtMissingOnPlayerViolation(usePacket.getEntityId()));
						}
					}
				}
			}
		}
		
		this.hadInteractAt = false;
	}
	
	protected HitData createHitData(Entity entity, IUseEntityIncomingPacket packet)
	{
		return new HitData(entity.getEntityId(), entity.getPos().immutable(), entity.isSleeping());
	}
	
	private void tick()
	{
		this.entities.int2ObjectEntrySet().forEach((e) -> e.getValue().tick(this));
		
		if (this.hits.isEmpty() || this.currentLocation == null || this.lastLocation == null)
		{
			return;
		}
		
		//We use last location, because we need to "tick" to figure out the entity location
		Helpers.Vec3 eyesLocation = Helpers.getPlayerHeadPos(this.lastLocation.getX(), this.lastLocation.getY(), this.lastLocation.getZ(), this.sneaking);
		
		//Vanilla has a bug where it uses lasts tick yaw, but some custom clients/mods have fixed this (and on newer mc versions) so check for that too
		Helpers.Vec3 headPosVanilla = Helpers.playerHeadPosReach(this.lastLocation.getYaw(), this.currentLocation.getPitch(), eyesLocation);
		Helpers.Vec3 headPosFixed = Helpers.playerHeadPosReach(this.currentLocation.getYaw(), this.currentLocation.getPitch(), eyesLocation);
		
		while (this.hits.size() > 0)
		{
			HitData hitData = this.hits.poll();
			
			//Used in debug mode, otherwise its just a NOP
			this.verifyEntityLocation(hitData);
			
			Position entityLocation = hitData.getTargetPosition();
			Helpers.AxisAlignedBBMutable hitbox = Helpers.buildEntityHitBox(entityLocation, hitData.isSmallHitbox());

			double reach = this.getReach(hitData, eyesLocation, headPosVanilla, headPosFixed, hitbox);
			
			UserViolation violation;
			if (reach == Double.NEGATIVE_INFINITY) //Our magic number for "not facing"
			{
				violation = new NotFacingAttackingEntityViolation(hitData.getEntityId(), this.lastLocation.immutable(), entityLocation);
			}
			else
			{
				if (!Boolean.FALSE.equals(this.getModule().inCreative())) //If we are not sure which gamemode we are on, assume creative
				{
					violation = reach > 6.0D ?
							new ReachViolation(hitData.getEntityId(), this.lastLocation.immutable(), entityLocation, reach, 6.0D)
							: null;
				}
				else
				{
					violation = reach > 3.0D ?
							new ReachViolation(hitData.getEntityId(), this.lastLocation.immutable(), entityLocation, reach, 3.0D)
							: null;
				}
			}
			
			if (violation != null)
			{
				this.addViolation(violation);
			}
		}
	}
	
	protected void verifyEntityLocation(HitData hitData)
	{
		//NOP
	}
	
	protected double getReach(HitData hitData, Helpers.Vec3 eyesLocation, Helpers.Vec3 headPosVanilla, Helpers.Vec3 headPosFixed, Helpers.AxisAlignedBBMutable hitbox)
	{
		Helpers.Vec3 vanillaHitVec = Helpers.getHitVec(eyesLocation, headPosVanilla, hitbox);
		Helpers.Vec3 fixedHitVec = Helpers.getHitVec(eyesLocation, headPosFixed, hitbox);
		
		return this.getMinNullable(this.getDistance(vanillaHitVec, eyesLocation), this.getDistance(fixedHitVec, eyesLocation));
	}
	
	protected final double getDistance(Helpers.Vec3 targetPos, Helpers.Vec3 playerHeadPos)
	{
		if (targetPos != null)
		{
			return playerHeadPos.distanceTo(targetPos);
		}
		else
		{
			return Double.NEGATIVE_INFINITY;
		}
	}
	
	protected final double getMinNullable(double value, double other)
	{
		if (value != Double.NEGATIVE_INFINITY && other != Double.NEGATIVE_INFINITY)
		{
			return Math.min(value, other);
		}
		else if (value != Double.NEGATIVE_INFINITY)
		{
			return value;
		}
		else if (other != Double.NEGATIVE_INFINITY)
		{
			return other;
		}
		
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IJoinOutgoingPacket)
		{
			this.handleJoinPacket((IJoinOutgoingPacket)packet);
		}
		else if (packet instanceof IRespawnOutgoingPacket)
		{
			this.handleRespawn((IRespawnOutgoingPacket)packet);
		}
		else if (packet instanceof ISpawnPlayerOutgoingPacket)
		{
			this.handlePlayerSpawn((ISpawnPlayerOutgoingPacket)packet);
		}
		else if (packet instanceof IDestroyEntitiesOutgoingPacket)
		{
			this.handleDestroyEntites((IDestroyEntitiesOutgoingPacket)packet);
		}
		else if (packet instanceof IEntityOutgoingPacket)
		{
			this.handleEntityMove((IEntityOutgoingPacket)packet);
		}
		else if (packet instanceof IEntityTeleportOutgoingPacket)
		{
			this.handleEntityTeleport((IEntityTeleportOutgoingPacket)packet);
		}
		else if (packet instanceof IEntityAttachOutgoingPacket)
		{
			this.handleAttachEntity((IEntityAttachOutgoingPacket)packet);
		}
		else if (packet instanceof ICameraOutgoingPacket)
		{
			this.handleCamera((ICameraOutgoingPacket)packet);
		}
		else if (packet instanceof IPlayerPositionAndLookOutgoingPacket)
		{
			this.handlePlayerTeleport((IPlayerPositionAndLookOutgoingPacket)packet);
		}
		else if (packet instanceof IUseBedOutgoingPacket)
		{
			this.handleUseBed((IUseBedOutgoingPacket)packet);
		}
		else if (packet instanceof IAnimationOutgoingPacket)
		{
			this.handleAnimation((IAnimationOutgoingPacket)packet);
		}
	}
	
	private void handleJoinPacket(IJoinOutgoingPacket packet)
	{
		this.changeWorld();
	}
	
	private void handleRespawn(IRespawnOutgoingPacket packet)
	{
		this.changeWorld();
	}
	
	private void changeWorld()
	{
		this.hadInteractAt = false;
		
		this.currentLocation = null;
		this.lastLocation = null;
		
		this.hits.clear();
		this.entities.clear();
		
		this.sneakingPending = null;
		this.sneaking = false;
	}

	private void handlePlayerSpawn(ISpawnPlayerOutgoingPacket packet)
	{
		Entity entity = this.buildEntity(packet.getEntityId());
		entity.setServerPos(packet.getServerX(), packet.getServerY(), packet.getServerZ());
		entity.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ());
		entity.reliableLocationSet();
		
		this.entities.put(packet.getEntityId(), entity);
	}
	
	protected Entity buildEntity(int id)
	{
		return new Entity(id);
	}
	
	private void handleDestroyEntites(IDestroyEntitiesOutgoingPacket packet)
	{	
		for(int entityId : packet.getEntityIds())
		{
			this.entities.remove(entityId);
		}
	}
	
	private void handleEntityMove(IEntityOutgoingPacket packet)
	{
		Entity entity = this.entities.get(packet.getEntityId());
		if (entity == null)
		{
			return;
		}

		if (!this.getModule().isInGame())
		{
			entity.unreliableLocationSet();
		}
		
		entity.addServerPos(packet.getServerX(), packet.getServerY(), packet.getServerZ());
		entity.setPositionAndRotation2((double)entity.getServerPosX() / 32.0D, (double)entity.getServerPosY() / 32.0D, (double)entity.getServerPosZ() / 32.0D, 3);
	}

	private void handleEntityTeleport(IEntityTeleportOutgoingPacket packet)
	{
		Entity entity = this.entities.get(packet.getEntityId());
		if (entity == null)
		{
			return;
		}

		if (!this.getModule().isInGame())
		{
			entity.unreliableLocationSet();
		}
		
		entity.setServerPos(packet.getServerX(), packet.getServerY(), packet.getServerZ());
		
		if (Math.abs(entity.getX() - packet.getX()) < 0.03125D && Math.abs(entity.getY() - packet.getY()) < 0.015625D && Math.abs(entity.getZ() - packet.getZ()) < 0.03125D)
		{
			entity.setPositionAndRotation2(entity.getX(), entity.getY(), entity.getZ(), 3);
		}
		else
		{
			entity.setPositionAndRotation2(packet.getX(), packet.getY(), packet.getZ(), 3);
		}
	}
	
	private void handleAttachEntity(IEntityAttachOutgoingPacket packet)
	{
		//If its the player that is being attached to other entity
		if (!Boolean.FALSE.equals(this.getModule().isCurrentEntity(packet.getEntityId())))
		{
			//We are getting attached, position is gonna be unstable
			if (packet.getVehicleId() != IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID)
			{
				this.hadInteractAt = false;
				
				this.currentLocation = null;
				this.lastLocation = null;
				
				this.hits.clear();
			}
			else if (!this.getModule().isCurrentRenderingEntity()) //We are getting unattached, but we are not current rendering entity
			{
				this.hadInteractAt = false;
				
				this.lastLocation = null;
				this.currentLocation = null;
				
				this.hits.clear();
				
				this.entities.int2ObjectEntrySet().forEach((e) -> e.getValue().unreliableLocationSet());
			}
		}
		
		Entity entity = this.entities.get(packet.getEntityId());
		if (entity == null)
		{
			return;
		}
		
		entity.setVehicleId(packet.getVehicleId());
	}
	
	private void handleCamera(ICameraOutgoingPacket packet)
	{
		if (!this.getModule().isCurrentRenderingEntity())
		{
			this.hadInteractAt = false;
			
			this.lastLocation = null;
			this.currentLocation = null;
			
			this.hits.clear();

			this.entities.int2ObjectEntrySet().forEach((e) -> e.getValue().unreliableLocationSet());
		}
	}
	
	private void handlePlayerTeleport(IPlayerPositionAndLookOutgoingPacket packet)
	{
		this.skipNextTick = true;
	}
	
	private void handleUseBed(IUseBedOutgoingPacket packet)
	{
		Entity entity = this.entities.get(packet.getEntityId());
		if (entity == null)
		{
			return;
		}
		
		entity.trySleep(packet.getPosition());
	}
	
	private void handleAnimation(IAnimationOutgoingPacket packet)
	{
		if (packet.getAnimation() != IAnimationOutgoingPacket.Animation.WAKE_UP)
		{
			return;
		}
		
		Entity entity = this.entities.get(packet.getEntityId());
		if (entity == null)
		{
			return;
		}
		
		entity.wakeUp();
	}
	
	public Entity getEntity(int id)
	{
		return this.entities.get(id);
	}
	
	public static Class<? extends Module> getRelevantClass()
	{
		if (PlayerInteractWithEntityModule.DEBUG)
		{
			return PlayerInteractWithEntityModuleDebug.class;
		}
		
		return PlayerInteractWithEntityModule.class; //Default
	}
	
	private static boolean getEnableDebug()
	{
		if (CheatDetectionMicroservice.DEBUG)
		{
			return System.getProperty("td.reach.debug", "false").equalsIgnoreCase("true");
		}
		
		return false; //Default
	}

	private static boolean getLenientCollisionBox()
	{
		if (PlayerInteractWithEntityModule.DEBUG)
		{
			String value = System.getProperty("td.reach.use-lenient-collision-box");
			if (value != null)
			{
				return value.equalsIgnoreCase("true");
			}
		}
		
		return true; //Default
	}
	
	public static class Helpers
	{
		private static AxisAlignedBBMutable buildEntityHitBox(IPosition targetLocation, boolean smallHitbox)
		{
	        final float width;
	        final float height;
	        if (smallHitbox) //On death, or sleeping
	        {
	        	width = 0.2F / 2.0F;
	        	height = 1.2F;
	        }
	        else
	        {
	        	width = 0.6F / 2.0F; //PLAYER: Default width when standing
	        	height = 1.8F; //PLAYER: Default height when standing
	        }
	        
	        AxisAlignedBBMutable hitDetectionBox = new AxisAlignedBBMutable(targetLocation.getX() - (double)width, targetLocation.getY(), targetLocation.getZ() - (double)width, targetLocation.getX() + (double)width, targetLocation.getY() + (double)height, targetLocation.getZ() + (double)width); //Get size that matches client

			final float collisionBorderSize;
			if (PlayerInteractWithEntityModule.COLLISION_BOX_LENIENT)
			{
				//0.1F is vanilla for all entities
				//0.045F Small movements won't get sent instantly, so increase the collision border a bit to allow for that
				//0.0045F Floating points ain't perfect
				collisionBorderSize = 0.1F + 0.045F + 0.0045F;
			}
			else
			{
				//0.1F is vanilla for all entities
				collisionBorderSize = 0.1F;
			}
	        
	        hitDetectionBox.expand((double)collisionBorderSize, (double)collisionBorderSize, (double)collisionBorderSize);
	        
	        return hitDetectionBox;
		}
		
		public static Vec3 getPlayerHeadPos(double x, double y, double z, boolean sneaking)
		{
			final float headHeight;
			if (sneaking)
			{
				headHeight = 1.62F - 0.08F;
			}
			else
			{
				headHeight = 1.62F; //PLAYER: Default head height when standing
			}
			
			return new Vec3(x, y + (double)headHeight, z);
		}
		
		public static Vec3 playerHeadPosReach(float yaw, float pitch, Vec3 headPos)
		{
			final double reach = Double.MAX_VALUE; //So we can check against any reach and determine whenever they hit far or they are not facing
			/*if (true)
			{
				reach = (double)4.5F; //PLAYER: Default survival reach
			}
			else
			{
				reach = (double)6.0F; //CREATIVE -> 6.0
			}*/
			//IS SLEEPING -> 0.2
			
			Vec3 lookPos = Helpers.f(pitch, yaw);
			
			return headPos.addVector(lookPos.xCoord * reach, lookPos.yCoord * reach, lookPos.zCoord * reach);
		}
		
		//Stolen from EntityRenderer
		public static Vec3 getHitVec(Vec3 playerHeadPos, Vec3 playerHeadPosReach, AxisAlignedBBMutable hitDetectionBox)
		{
			Vec3 hitVec = hitDetectionBox.calculateIntercept(playerHeadPos, playerHeadPosReach);

			Vec3 targetPos = null;
			if (hitDetectionBox.isVecInside(playerHeadPos))
			{
				targetPos = hitVec == null ? playerHeadPos : hitVec;
			}
			else if (hitVec != null)
			{
				targetPos = hitVec;
			}
			
			return targetPos;
		}

		//Stolen from Entity
		private static Vec3 f(float pitch, float yaw)
		{
	        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
	        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
	        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
	        float f3 = MathHelper.sin(-pitch * 0.017453292F);
	        
	        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
		}
		
		public static class Vec3
		{
		    public final double xCoord;
		    public final double yCoord;
		    public final double zCoord;

		    public Vec3(double x, double y, double z)
		    {
		        if (x == -0.0D)
		        {
		            x = 0.0D;
		        }

		        if (y == -0.0D)
		        {
		            y = 0.0D;
		        }

		        if (z == -0.0D)
		        {
		            z = 0.0D;
		        }

		        this.xCoord = x;
		        this.yCoord = y;
		        this.zCoord = z;
		    }

		    public Vec3 addVector(double x, double y, double z)
		    {
		        return new Vec3(this.xCoord + x, this.yCoord + y, this.zCoord + z);
		    }
		    
		    public double distanceTo(Vec3 vec)
		    {
		        double d0 = vec.xCoord - this.xCoord;
		        double d1 = vec.yCoord - this.yCoord;
		        double d2 = vec.zCoord - this.zCoord;
		        
		        return (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
		    }
		    
		    public Vec3 getIntermediateWithXValue(Vec3 vec, double x)
		    {
		        double d0 = vec.xCoord - this.xCoord;
		        double d1 = vec.yCoord - this.yCoord;
		        double d2 = vec.zCoord - this.zCoord;

		        if (d0 * d0 < 1.0000000116860974E-7D)
		        {
		            return null;
		        }
		        else
		        {
		            double d3 = (x - this.xCoord) / d0;
		            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
		        }
		    }

		    public Vec3 getIntermediateWithYValue(Vec3 vec, double y)
		    {
		        double d0 = vec.xCoord - this.xCoord;
		        double d1 = vec.yCoord - this.yCoord;
		        double d2 = vec.zCoord - this.zCoord;

		        if (d1 * d1 < 1.0000000116860974E-7D)
		        {
		            return null;
		        }
		        else
		        {
		            double d3 = (y - this.yCoord) / d1;
		            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
		        }
		    }

		    public Vec3 getIntermediateWithZValue(Vec3 vec, double z)
		    {
		        double d0 = vec.xCoord - this.xCoord;
		        double d1 = vec.yCoord - this.yCoord;
		        double d2 = vec.zCoord - this.zCoord;

		        if (d2 * d2 < 1.0000000116860974E-7D)
		        {
		            return null;
		        }
		        else
		        {
		            double d3 = (z - this.zCoord) / d2;
		            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(this.xCoord + d0 * d3, this.yCoord + d1 * d3, this.zCoord + d2 * d3) : null;
		        }
		    }

		    public double squareDistanceTo(Vec3 vec)
		    {
		        double d0 = vec.xCoord - this.xCoord;
		        double d1 = vec.yCoord - this.yCoord;
		        double d2 = vec.zCoord - this.zCoord;
		        
		        return d0 * d0 + d1 * d1 + d2 * d2;
		    }

		    public String toString()
		    {
		        return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
		    }
		}
		
		public static class AxisAlignedBBMutable
		{
		    public double minX;
		    public double minY;
		    public double minZ;
		    
		    public double maxX;
		    public double maxY;
		    public double maxZ;

		    public AxisAlignedBBMutable(double x1, double y1, double z1, double x2, double y2, double z2)
		    {
		        this.minX = Math.min(x1, x2);
		        this.minY = Math.min(y1, y2);
		        this.minZ = Math.min(z1, z2);
		        
		        this.maxX = Math.max(x1, x2);
		        this.maxY = Math.max(y1, y2);
		        this.maxZ = Math.max(z1, z2);
		    }

		    public void expand(double x, double y, double z)
		    {
		        double x1 = this.minX - x;
		        double y1 = this.minY - y;
		        double z1 = this.minZ - z;
		        double x2 = this.maxX + x;
		        double y2 = this.maxY + y;
		        double z2 = this.maxZ + z;
		        
		        this.minX = Math.min(x1, x2);
		        this.minY = Math.min(y1, y2);
		        this.minZ = Math.min(z1, z2);
		        
		        this.maxX = Math.max(x1, x2);
		        this.maxY = Math.max(y1, y2);
		        this.maxZ = Math.max(z1, z2);
		    }

		    public Vec3 calculateIntercept(Vec3 vecA, Vec3 vecB)
		    {
		        Vec3 vec3 = vecA.getIntermediateWithXValue(vecB, this.minX);
		        if (!this.isVecInYZ(vec3))
		        {
		            vec3 = null;
		        }

		        Vec3 vec31 = vecA.getIntermediateWithXValue(vecB, this.maxX);
		        if (!this.isVecInYZ(vec31))
		        {
		            vec31 = null;
		        }

		        Vec3 vec32 = vecA.getIntermediateWithYValue(vecB, this.minY);
		        if (!this.isVecInXZ(vec32))
		        {
		            vec32 = null;
		        }

		        Vec3 vec33 = vecA.getIntermediateWithYValue(vecB, this.maxY);
		        if (!this.isVecInXZ(vec33))
		        {
		            vec33 = null;
		        }

		        Vec3 vec34 = vecA.getIntermediateWithZValue(vecB, this.minZ);
		        if (!this.isVecInXY(vec34))
		        {
		            vec34 = null;
		        }

		        Vec3 vec35 = vecA.getIntermediateWithZValue(vecB, this.maxZ);
		        if (!this.isVecInXY(vec35))
		        {
		            vec35 = null;
		        }

		        Vec3 vec36 = null;

		        if (vec3 != null)
		        {
		            vec36 = vec3;
		        }

		        if (vec31 != null && (vec36 == null || vecA.squareDistanceTo(vec31) < vecA.squareDistanceTo(vec36)))
		        {
		            vec36 = vec31;
		        }

		        if (vec32 != null && (vec36 == null || vecA.squareDistanceTo(vec32) < vecA.squareDistanceTo(vec36)))
		        {
		            vec36 = vec32;
		        }

		        if (vec33 != null && (vec36 == null || vecA.squareDistanceTo(vec33) < vecA.squareDistanceTo(vec36)))
		        {
		            vec36 = vec33;
		        }

		        if (vec34 != null && (vec36 == null || vecA.squareDistanceTo(vec34) < vecA.squareDistanceTo(vec36)))
		        {
		            vec36 = vec34;
		        }

		        if (vec35 != null && (vec36 == null || vecA.squareDistanceTo(vec35) < vecA.squareDistanceTo(vec36)))
		        {
		            vec36 = vec35;
		        }

	            return vec36;
		    }
		    
		    public boolean isVecInside(Vec3 vec)
		    {
		        return vec.xCoord > this.minX && vec.xCoord < this.maxX ? (vec.yCoord > this.minY && vec.yCoord < this.maxY ? vec.zCoord > this.minZ && vec.zCoord < this.maxZ : false) : false;
		    }

		    private boolean isVecInYZ(Vec3 vec)
		    {
		        return vec == null ? false : vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
		    }
		    
		    private boolean isVecInXZ(Vec3 vec)
		    {
		        return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
		    }
		    
		    private boolean isVecInXY(Vec3 vec)
		    {
		        return vec == null ? false : vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
		    }

		    public String toString()
		    {
		        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
		    }
		}
	    
	    private static class MathHelper
	    {
	        private static final float[] SIN_TABLE = new float[65536];
	        
	        public static float sin(float p_76126_0_)
	        {
	            return SIN_TABLE[(int)(p_76126_0_ * 10430.378F) & 65535];
	        }

	        public static float cos(float value)
	        {
	            return SIN_TABLE[(int)(value * 10430.378F + 16384.0F) & 65535];
	        }

	        public static float sqrt_double(double value)
	        {
	            return (float)Math.sqrt(value);
	        }

	        static
	        {
	            for (int i = 0; i < 65536; ++i)
	            {
	                SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0D / 65536.0D);
	            }
	        }
	    }
	}

	public static class Entity
	{
		@Getter private final int entityId;
		
		private PositionMutable pos;
		
		@Getter private int serverPosX;
		@Getter private int serverPosY;
		@Getter private int serverPosZ;
		
		@Getter private double moveToX;
		@Getter private double moveToY;
		@Getter private double moveToZ;
		@Getter private int moveToIncrements;
		
		@Getter private int vehicleId = -1;
		
		@Getter private boolean sleeping;
		
		private boolean reliablePositionSet;
		
		protected Entity(int entityId)
		{
			this.entityId = entityId;
			
			this.pos = new PositionMutable();
		}
		
		private void setVehicleId(int vehicleId)
		{
			if (this.vehicleId == -1 && vehicleId == -1) //Nothing to do
			{
				return;
			}
			
			this.vehicleId = vehicleId;
			
			if (vehicleId == -1)
			{
				this.unreliableLocationSet();
			}
		}
		
		private void setServerPos(int x, int y, int z)
		{
			this.serverPosX = x;
			this.serverPosY = y;
			this.serverPosZ = z;
		}
		
		private void addServerPos(int x, int y, int z)
		{
			this.serverPosX += x;
			this.serverPosY += y;
			this.serverPosZ += z;
		}

		private void setPosition(double x, double y, double z)
		{
			this.pos.setPosition(x, y, z);
		}
		
		private void setPositionAndRotation(double x, double y, double z)
		{
            this.setPosition(x, y, z);
		}
		
		private void setPositionAndRotation2(double x, double y, double z, int posRotationIncrements)
		{
			this.moveToX = x;
			this.moveToY = y;
			this.moveToZ = z;
			this.moveToIncrements = posRotationIncrements;
		}
		
		protected void tick(PlayerInteractWithEntityModule module)
		{
			if (this.moveToIncrements > 0)
			{
	            double x = this.getX() + (this.moveToX - this.getX()) / (double)this.moveToIncrements;
	            double y = this.getY() + (this.moveToY - this.getY()) / (double)this.moveToIncrements;
	            double z = this.getZ() + (this.moveToZ - this.getZ()) / (double)this.moveToIncrements;
	            
	            this.moveToIncrements--;
	            
	            this.setPosition(x, y, z);
			}
		}
		
		private void trySleep(IBlockPos location)
		{
			//Going to bed literally sets the position, this is good for us so we can have set point where the player is
			
			this.sleeping = true;
			
			this.unreliableLocationSet();
			
			/*if (false) //IF BLOCK LOADED
			{
	            float f = 0.5F;
	            float f1 = 0.5F;
	            
	            switch(facing)
	            {
	            	SOUTH -> f1 = 0.9F;
	            	NORTH -> f1 = 0.1F;
	            	WEST -> f = 0.1F;
	            	EAST -> f = 0.9F;
	            }
	            
				this.setPosition((double)((float) location.getX() + f), (double)((float) location.getY() + 0.6875F), (double)((float) location.getZ() + f1));
			}*/
			
			//Set bed location
			
			//this.setPosition((double)((float) location.getX() + 0.5F), (double)((float) location.getY() + 0.6875F), (double)((float) location.getZ() + 0.5F));
		}
		
		private void wakeUp()
		{
			//Waking up literally sets the position, this is good for us so we can have set point where the player is
			
			this.sleeping = false;
			
			this.unreliableLocationSet();
			
			//Find block from bed location (if not null), and if bed
			//Find safe location
			//If no -> bed location, y + 1
			//Otherwise -> spawn in that block, x + 0.5, y + 0.1, y + 0.5
		}
		
		private void unreliableLocationSet()
		{
			this.reliablePositionSet = false;
		}
		
		private void reliableLocationSet()
		{
			this.reliablePositionSet = true;
		}
		
		protected boolean isReliableLocation()
		{
			return this.vehicleId == -1 ? this.reliablePositionSet : false;
		}
		
		public double getX()
		{
			return this.pos.getX();
		}
		
		public double getY()
		{
			return this.pos.getY();
		}
		
		public double getZ()
		{
			return this.pos.getZ();
		}
		
		public IPosition getPos()
		{
			return this.pos;
		}
	}
	
	@RequiredArgsConstructor
	protected static class HitData
	{
		@Getter private final int entityId;
		@Getter private final Position targetPosition;
		
		//State data
		@Getter private final boolean smallHitbox;
	}
}
