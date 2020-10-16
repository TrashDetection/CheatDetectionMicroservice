package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.combat.debug;

import fi.joniaromaa.cheatdetectionmicroservice.user.data.LocationMutable;
import fi.joniaromaa.cheatdetectionmicroservice.user.data.Position;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.combat.PlayerInteractWithEntityModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.inspect.InspectInformationViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket.DebugEntityAttack;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket.DebugEntityMove;
import fi.joniaromaa.minecrafthook.common.network.incoming.IUseEntityIncomingPacket;
import fi.joniaromaa.minecrafthook.common.utils.IVectorPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;

public class PlayerInteractWithEntityModuleDebug extends PlayerInteractWithEntityModule
{
	private static final HeadPosTracking HEAD_POS_TRACKING = PlayerInteractWithEntityModuleDebug.getSelectedHeadPosTracking(); //Only track fixed head position
	
	private DebugEntityAttack debugEntityAttack;
	private Int2ObjectMap<DebugEntityMove> debugEntityMove;
	
	public PlayerInteractWithEntityModuleDebug(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);

		this.debugEntityMove = new Int2ObjectOpenHashMap<>();
	}
	
	@Override
	public boolean handlesPluginMessages()
	{
		return true;
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IPluginMessageIncomingPacket)
		{
			IPluginMessageIncomingPacket pluginMessagePacket = (IPluginMessageIncomingPacket)packet;
			
			if (pluginMessagePacket.getDataObject() instanceof DebugEntityAttack)
			{
				this.debugEntityAttack = (DebugEntityAttack)pluginMessagePacket.getDataObject();
			}
			else if (pluginMessagePacket.getDataObject() instanceof DebugEntityMove)
			{
				DebugEntityMove entityMove = (DebugEntityMove)pluginMessagePacket.getDataObject();
				
				this.debugEntityMove.put(entityMove.getEntityId(), entityMove);
			}
			
			return;
		}
		
		super.analyzeIncoming(packet);
	}
	
	@Override
	protected HitData createHitData(PlayerInteractWithEntityModule.Entity entity, IUseEntityIncomingPacket packet)
	{
		return new HitData(entity.getEntityId(), entity.getPos().immutable(), entity.isSleeping(), this.debugEntityAttack, packet.getAction() == IUseEntityIncomingPacket.Action.INTERACT_AT ? packet.getPosition() : null);
	}

	@Override
	protected Entity buildEntity(int id)
	{
		return new Entity(id);
	}
	
	@Override
	protected void verifyEntityLocation(PlayerInteractWithEntityModule.HitData hitData)
	{
		this.verifyEntityLocationInternal((HitData)hitData);
	}
	
	private void verifyEntityLocationInternal(HitData hitData)
	{
		DebugEntityAttack debug = hitData.getDebug();
		if (debug != null)
		{
			Position entityPosition = hitData.getTargetPosition();
			
			if (debug.getX() != entityPosition.getX() || debug.getY() != entityPosition.getY() || debug.getZ() != entityPosition.getZ())
			{
				this.addViolation(new InspectInformationViolation(String.format("Entity Location Wrong | Entity Id: %d | X Was: %s | Y Was: %s | Z Was: %s | X Expected: %s | Y Expected: %s | Z Expected: %s", hitData.getEntityId(), entityPosition.getX(), entityPosition.getY(), entityPosition.getZ(), debug.getX(), debug.getY(), debug.getZ())));
			}
			
			LocationMutable location = this.getLastLocation();
			
			if (debug.getPlayerX() != location.getX() || debug.getPlayerY() != location.getY() || debug.getPlayerZ() != location.getZ())
			{
				this.addViolation(new InspectInformationViolation(String.format("Player Location Wrong | Entity Id: %d | X Was: %s | Y Was: %s | Z Was: %s | X Expected: %s | Y Expected: %s | Z Expected: %s", hitData.getEntityId(), location.getX(), location.getY(), location.getZ(), debug.getPlayerX(), debug.getPlayerY(), debug.getPlayerZ())));
			}
			
			if (debug.getPlayerYaw() != location.getYaw() || debug.getPlayerPitch() != location.getPitch())
			{
				this.addViolation(new InspectInformationViolation(String.format("Player Look Wrong | Entity Id: %d | Yaw Was: %f | Pitch Was: %f | Yaw Excepted: %f | Pitch Excepted: %f", hitData.getEntityId(), location.getYaw(), location.getPitch(), debug.getPlayerYaw(), debug.getPlayerPitch())));
			}
		}
	}
	
	@Override
	protected double getReach(PlayerInteractWithEntityModule.HitData hitData, Helpers.Vec3 eyesLocation, Helpers.Vec3 headPosVanilla, Helpers.Vec3 headPosFixed, Helpers.AxisAlignedBBMutable hitbox)
	{
		return this.getReachInternal((HitData)hitData, eyesLocation, headPosVanilla, headPosFixed, hitbox);
	}
	
	private double getReachInternal(HitData hitData, Helpers.Vec3 eyesLocation, Helpers.Vec3 headPosVanilla, Helpers.Vec3 headPosFixed, Helpers.AxisAlignedBBMutable hitbox)
	{
		if (PlayerInteractWithEntityModuleDebug.HEAD_POS_TRACKING == HeadPosTracking.VANILLA_ONLY)
		{
			Helpers.Vec3 vanillaHitVec = Helpers.getHitVec(eyesLocation, headPosVanilla, hitbox);
			
			if (hitData.getInteractAt() != null)
			{
				this.hitPosMatchesInteractPos(hitData.getEntityId(), hitData.getTargetPosition(), vanillaHitVec, hitData.getInteractAt());
			}
			
			return this.getDistance(vanillaHitVec, eyesLocation);
		}
		else if (PlayerInteractWithEntityModuleDebug.HEAD_POS_TRACKING == HeadPosTracking.FIXED_ONLY)
		{
			Helpers.Vec3 fixedHitVec = Helpers.getHitVec(eyesLocation, headPosFixed, hitbox);

			if (hitData.getInteractAt() != null)
			{
				this.hitPosMatchesInteractPos(hitData.getEntityId(), hitData.getTargetPosition(), fixedHitVec, hitData.getInteractAt());
			}
			
			return this.getDistance(fixedHitVec, eyesLocation);
		}
		else
		{
			Helpers.Vec3 vanillaHitVec = Helpers.getHitVec(eyesLocation, headPosVanilla, hitbox);
			Helpers.Vec3 fixedHitVec = Helpers.getHitVec(eyesLocation, headPosFixed, hitbox);
			
			if (hitData.getInteractAt() != null)
			{
				this.hitPosMatchesInteractPos(hitData.getEntityId(), hitData.getTargetPosition(), vanillaHitVec, hitData.getInteractAt());
				this.hitPosMatchesInteractPos(hitData.getEntityId(), hitData.getTargetPosition(), fixedHitVec, hitData.getInteractAt());
			}
			
			return this.getMinNullable(this.getDistance(vanillaHitVec, eyesLocation), this.getDistance(fixedHitVec, eyesLocation));
		}
	}

	private void hitPosMatchesInteractPos(int entityId, Position entityPosition, Helpers.Vec3 hitPos, IVectorPos interactPos)
	{
		if (hitPos != null)
		{
			double x = (float)(hitPos.xCoord - entityPosition.getX());
			double y = (float)(hitPos.yCoord - entityPosition.getY());
			double z = (float)(hitPos.zCoord - entityPosition.getZ());
			
			if (interactPos.getX() != x || interactPos.getY() != y || interactPos.getZ() != z)
			{
				this.addViolation(new InspectInformationViolation(String.format("Hit Pos Not Matching Interact At | Entity Id: %d | Interact Pos Client: %s | Interact Pos Server: %s", entityId, interactPos, new Position(x, y, z))));
			}
		}
		else
		{
			this.addViolation(new InspectInformationViolation(String.format("Hit Pos Missed But Interact At | Entity Id: %d", entityId)));
		}
	}
	
	private static HeadPosTracking getSelectedHeadPosTracking()
	{
		if (PlayerInteractWithEntityModule.DEBUG)
		{
			String value = System.getProperty("td.reach.head-pos-tracking");
			if (value != null)
			{
				if (value.equalsIgnoreCase("vanilla"))
				{
					return HeadPosTracking.VANILLA_ONLY;
				}
				else if (value.equalsIgnoreCase("fixed"))
				{
					return HeadPosTracking.FIXED_ONLY;
				}
			}
		}
		
		return HeadPosTracking.ALL; //Default
	}

	public static class Entity extends PlayerInteractWithEntityModule.Entity
	{
		public Entity(int entityId)
		{
			super(entityId);
		}
		
		@Override
		protected void tick(PlayerInteractWithEntityModule module)
		{
			this.tickDebug((PlayerInteractWithEntityModuleDebug)module);
			
			super.tick(module);
		}
		
		private void tickDebug(PlayerInteractWithEntityModuleDebug module)
		{
			if (this.isReliableLocation())
			{
				DebugEntityMove debug = module.debugEntityMove.get(this.getEntityId());
				if (debug != null && (debug.getX() != this.getX() || debug.getY() != this.getY() || debug.getZ() != this.getZ()))
				{
					module.addViolation(new InspectInformationViolation(String.format("Entity Move Was Wrong | Entity Id: %d | X Was: %s | Y Was: %s | Z Was: %s | X Expected: %s | Y Expected: %s | Z Expected: %s", this.getEntityId(), this.getX(), this.getY(), this.getZ(), debug.getX(), debug.getY(), debug.getZ())));
				}
			}
		}
	}
	
	protected static class HitData extends PlayerInteractWithEntityModule.HitData
	{
		public HitData(int entityId, Position targetPosition, boolean smallHitbox, DebugEntityAttack debug, IVectorPos interactAt)
		{
			super(entityId, targetPosition, smallHitbox);
			
			this.debug = debug;
			
			this.interactAt = interactAt;
		}

		@Getter private final DebugEntityAttack debug;
		
		@Getter private final IVectorPos interactAt;
	}
	
	private static enum HeadPosTracking
	{
		ALL,
		VANILLA_ONLY,
		FIXED_ONLY
	}
}
