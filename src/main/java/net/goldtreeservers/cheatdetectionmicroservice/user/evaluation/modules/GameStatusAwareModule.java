package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules;

import java.util.IdentityHashMap;
import java.util.Map;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.game.entity.attribute.IAttributeInstance;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.game.entity.attribute.IAttributeMap;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.game.entity.attribute.IAttributeModifier;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerAbilitiesIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPluginMessageIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IAnimationOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IBulkChunkOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IChangeGameStateOutgoingPacaket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IChunkOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityAttachOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IEntityPropertiesOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPlayerAbilitiesOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPlayerPositionAndLookOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IEntityPropertySnapshot;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IGamemode;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.inspect.InspectInformationViolation;

public final class GameStatusAwareModule extends ConfirmationModule
{							
	private static final int INVALID_ENTITY_ID = -1693666569;
	
	private static final byte CHUNK_NOT_FOUND = 0;
	private static final byte CHUNK_FOUND = 1;
	
	@Getter private IGamemode gamemode;
	
	@Getter private Integer entityId;
	private Integer cameraId;
	private int vehicleId;

	@Getter private Boolean sneaking;

	private SprintStatus sprintStatus;
	
	@Getter private IAttributeMap attributes;
	
	private IPlayerIncomingPacket lastActualMove;
	@Getter private IPlayerAbilitiesOutgoingPacket serverAbilities;

	@Getter private Boolean invulnerable;
	@Getter private Boolean flying;
	
	@Getter private boolean inGame;
	@Getter private boolean justTeleported;
	
	private IPlayerPositionAndLookOutgoingPacket waitingTeleportPacket;
	
	private Long2ByteMap chunks;
	
	private Map<Class<? extends GameStatusAwareSubmodule>, GameStatusAwareSubmodule> submodules;
	
	public GameStatusAwareModule(UserEvaluation userEvaluation)
	{
		super(userEvaluation);
		
		this.cameraId = GameStatusAwareModule.INVALID_ENTITY_ID; //We need to assume we are rendering as someone else before stated otherwise
		this.vehicleId = GameStatusAwareModule.INVALID_ENTITY_ID; //We need to assume we are attached to vehicle before stated otherwise
		
		this.inGame = false;
		
		this.chunks = new Long2ByteOpenHashMap();
		
		this.submodules = new IdentityHashMap<>();
		
		this.addConfirmPacket(IJoinOutgoingPacket.class, this::handleJoin);
		this.addConfirmPacket(IRespawnOutgoingPacket.class, this::handleRespawn);
		this.addConfirmPacket(IChangeGameStateOutgoingPacaket.class, this::handleGameStateChange, true);
		this.addConfirmPacket(IAnimationOutgoingPacket.class, true);
		this.addConfirmPacket(ICameraOutgoingPacket.class, this::handleCamera);
		this.addConfirmPacket(IEntityAttachOutgoingPacket.class, this::handleAttach);
		this.addConfirmPacket(IPlayerPositionAndLookOutgoingPacket.class, this::handleTeleport);
		this.addConfirmPacket(IChunkOutgoingPacket.class, this::handleChunk);
		this.addConfirmPacket(IBulkChunkOutgoingPacket.class, this::handleBulkChunk);
		this.addConfirmPacket(IPlayerAbilitiesOutgoingPacket.class, this::serverAbilities);
		this.addConfirmPacket(IEntityPropertiesOutgoingPacket.class, this::handleProperties);
		
		this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::handlePlayerPre);
		this.addIncomingHandlerPre(IPlayerAbilitiesIncomingPacket.class, this::clientAbilities);
		this.addIncomingHandlerPre(IIncomingEntityActionIncomingPacket.class, this::handleAction);
		
		this.addIncomingHandlerPost(IPlayerIncomingPacket.class, this::handlePlayerPost);
	}

	@Override
	public boolean handlesPluginMessages()
	{
		return true;
	}
	
	public void addSubmodule(GameStatusAwareSubmodule module)
	{
		this.submodules.put(module.getClass(), module);
	}
	
	@Override
	protected boolean shouldConfirmSpecial(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IChangeGameStateOutgoingPacaket)
		{
			return ((IChangeGameStateOutgoingPacaket)packet).getState() == IChangeGameStateOutgoingPacaket.State.SET_GAMEMODE;
		}
		
		if (packet instanceof IAnimationOutgoingPacket)
		{
			IAnimationOutgoingPacket animation = (IAnimationOutgoingPacket)packet;
			if (animation.getAnimation() == IAnimationOutgoingPacket.Animation.WAKE_UP)
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected void analyzeIncoming0(IMinecraftIncomingPacket packet)
	{
		this.submodules.values().forEach((m) ->
		{
			if (!m.handlesPluginMessages() && packet instanceof IPluginMessageIncomingPacket)
			{
				return;
			}
			
			m.analyzeIncoming(packet);
		});
	}

	@Override
	protected void analyzeOutgoing0(IMinecraftOutgoingPacket packet)
	{
		this.submodules.values().forEach((m) -> m.analyzeOutgoing(packet));
	}

	@Override
	protected void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		super.packetConfirmed(packet);
		
		this.submodules.values().forEach((m) -> m.packetConfirmed(packet));
	}

	@Override
	protected void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		this.submodules.values().forEach((m) -> m.analyzeConfirmation(confirmed, packet));
	}
	
	private void handleJoin(IJoinOutgoingPacket packet)
	{
		this.flying = false;
		
		this.setGamemode(packet.getGamemode());

		this.entityId = packet.getEntityId();
		this.cameraId = null; //We are in current entity id, null
		this.vehicleId = IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID; //We aren't attached to vehicle
		
		this.sneaking = false;

		this.sprintStatus = SprintStatus.NOT_SPRINTING;
		
		this.inGame = false;
		
		this.chunks.clear();
		
		this.initAttributes(true);
	}
	
	private void handleRespawn(IRespawnOutgoingPacket packet)
	{
		this.flying = false;
		
		this.setGamemode(packet.getGamemode());
		
		this.cameraId = null; //We are in current entity id, null
		this.vehicleId = IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID; //We aren't attached to vehicle
		
		this.sneaking = false;
		this.sprintStatus = SprintStatus.NOT_SPRINTING;
		
		this.inGame = false;
		
		this.chunks.clear();
		
		this.initAttributes(true);
	}

	private void initAttributes()
	{
		this.initAttributes(false);
	}
	
	private void initAttributes(boolean clear)
	{
		if (this.attributes == null || clear)
		{
			this.attributes = this.getEvaluation().getUser().getHook().getGameHook().getEntityHook().getAttributeHook().createAttributeMap();
			this.attributes.registerAttribute(this.getEvaluation().getUser().getHook().getGameHook().getEntityHook().getAttributeHook().getSharedMovementSpeedAttribute());
		}
	}
	
	private void handleGameStateChange(IChangeGameStateOutgoingPacaket packet)
	{
		if (packet.getState() == IChangeGameStateOutgoingPacaket.State.SET_GAMEMODE) //Game mode change
		{
			IGamemode.Type type = IGamemode.Type.valueOf((int)packet.getValue());
			IGamemode mode = this.getEvaluation().getUser().getHook().getGameHook().getWorldHook().getGamemode(type);
			
			this.setGamemode(mode);
		}
	}
	
	private void handleCamera(ICameraOutgoingPacket packet)
	{
		//TODO: Should we keep track of valid entities? Or assume its all good...
		
		this.cameraId = packet.getEntityId();
	}
	
	private void handleAttach(IEntityAttachOutgoingPacket packet)
	{
		if (this.entityId != null)
		{
			//If the entity id is the current player entity id, then we are being attached
			if (this.entityId.equals(packet.getEntityId()))
			{
				this.vehicleId = packet.getVehicleId();
			}
		}
		else if (packet.getVehicleId() != IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID) //Entity id is null and someone is being attached, we have to assume its us
		{
			this.vehicleId = GameStatusAwareModule.INVALID_ENTITY_ID;
		}
	}
	
	private void handleTeleport(IPlayerPositionAndLookOutgoingPacket packet)
	{
		this.waitingTeleportPacket = packet;
	}
	
	private void handleChunk(IChunkOutgoingPacket packet)
	{
		if (packet.isFullChunk())
		{
			if (packet.getExtractedDataLength() == 0)
			{
				this.chunks.remove(packet.getChunkPair());
				
				//When we are unloading, we remove all entities, if the player is inside the chunk, it gets removed too
				if (this.lastActualMove != null)
				{
					if (this.lastActualMove.getChunkX() == packet.getChunkX() && this.lastActualMove.getChunkZ() == packet.getChunkZ())
					{
						this.playerRemovedFromChunk();
					}
				}
				
				return;
			}
			
			byte old = this.chunks.put(packet.getChunkPair(), GameStatusAwareModule.CHUNK_FOUND);
			if (old != GameStatusAwareModule.CHUNK_NOT_FOUND)
			{
				this.addViolation(new InspectInformationViolation(String.format("Chunk Corruption | X: %d | Z: %d", packet.getChunkX(), packet.getChunkZ())));
			}
		}
	}
	
	protected void playerRemovedFromChunk()
	{
		this.inGame = false;
		
		this.submodules.values().forEach((m) -> m.playerRemovedFromChunk());
	}
	
	private void handleBulkChunk(IBulkChunkOutgoingPacket packet)
	{
		for(int i = 0; i < packet.getCount(); i++)
		{
			byte old = this.chunks.put(packet.getChunkPair(i), GameStatusAwareModule.CHUNK_FOUND);
			if (old != GameStatusAwareModule.CHUNK_NOT_FOUND)
			{
				this.addViolation(new InspectInformationViolation(String.format("Chunk Corruption (Bulk) | X: %d | Z: %d", packet.getChunkX(i), packet.getChunkZ(i))));
			}
		}
	}
	
	private void handlePlayerPre(IPlayerIncomingPacket packet)
	{
		if (this.waitingTeleportPacket == null)
		{
			return;
		}
		
		if (packet.isOnGround())
		{
			return; //The teleport packet's onGround is always false
		}
		
		if (packet.getX() != this.waitingTeleportPacket.getX() || packet.getY() != this.waitingTeleportPacket.getY() || packet.getZ() != this.waitingTeleportPacket.getZ())
		{
			return; //If the coords don't match up, not this
		}

		this.waitingTeleportPacket = null;
		this.justTeleported = true;
	}
	
	private void handlePlayerPost(IPlayerIncomingPacket packet)
	{
		if (this.justTeleported)
		{
			this.justTeleported = false;
		}
		else
		{
			this.inGame = true;
			
			if (this.sprintStatus != null)
			{
				switch(this.sprintStatus)
				{
					case START_SPRINTING:
						this.sprintStatus = SprintStatus.SPRINTING;
						break;
					case STOP_SPRINTING:
						this.sprintStatus = SprintStatus.NOT_SPRINTING;
						break;
					default:
						break;
				}
			}
			
			if (packet.isMoving())
			{
				this.lastActualMove = packet;
			}
		}
	}
	
	private void serverAbilities(IPlayerAbilitiesOutgoingPacket packet)
	{
		this.serverAbilities = packet;

		this.invulnerable = packet.isInvulnerable();
		this.flying = packet.isFlying();
	}
	
	private void handleProperties(IEntityPropertiesOutgoingPacket packet)
	{
		if (this.entityId == null || !this.entityId.equals(packet.getEntityId()))
		{
			return;
		}
		
		this.initAttributes();
		
		for(IEntityPropertySnapshot snapshot : packet.getProperties())
		{
			IAttributeInstance instance = this.attributes.getAttributeInstanceByName(snapshot.getName());
			if (instance == null)
			{
				continue; //Only known attributes
			}
			
			instance.setBaseValue(snapshot.getInitialValue());
			instance.removeAllModifiers();
			
			for(IAttributeModifier modifier : snapshot.getModifiers())
			{
				instance.applyModifier(modifier);
			}
		}
	}
	
	private void clientAbilities(IPlayerAbilitiesIncomingPacket packet)
	{
		this.flying = packet.isFlying();
	}
	
	private void handleAction(IIncomingEntityActionIncomingPacket packet)
	{
		switch(packet.getAction())
		{
			case START_SPRINTING:
				this.sprintStatus = SprintStatus.START_SPRINTING;
				this.setSprinting(true);
				break;
			case STOP_SPRINTING:
				this.sprintStatus = SprintStatus.STOP_SPRINTING;
				this.setSprinting(false);
				break;
			default:
				break;
		}
	}
	
	private void setSprinting(boolean sprinting)
	{
		if (this.attributes == null)
		{
			return;
		}
		
		IAttributeModifier modifier = this.getEvaluation().getUser().getHook().getGameHook().getEntityHook().getSprintingSpeedBoostModifier();
		
		IAttributeInstance instance = this.attributes.getAttributeInstance(this.getEvaluation().getUser().getHook().getGameHook().getEntityHook().getAttributeHook().getSharedMovementSpeedAttribute());
		if (instance.hasModifier(modifier))
		{
			instance.removeModifier(modifier);
		}
		
		if (sprinting)
		{
			instance.applyModifier(modifier);
		}
	}
	
	private void setGamemode(IGamemode gamemode)
	{
		this.gamemode = gamemode;
		
		if (gamemode.isCreative())
		{
			this.invulnerable = true;
		}
		else if (gamemode.isSpectator())
		{
			this.invulnerable = true;
			this.flying = true;
		}
		else
		{
			this.invulnerable = false;
			this.flying = false;
		}
	}
	
	public Boolean inCreative()
	{
		if (this.gamemode == null)
		{
			return null;
		}
		
		return this.gamemode.isCreative();
	}
	
	public Boolean isSpectator()
	{
		if (this.gamemode == null)
		{
			return null;
		}
		
		return this.gamemode.isSpectator();
	}
	
	public boolean isCurrentRenderingEntity()
	{
		if (this.cameraId == null)
		{
			return true;
		}
		else if (this.entityId == null)
		{
			return false;
		}
		
		return this.entityId.equals(this.cameraId);
	}
	
	public boolean inVehicle()
	{
		return this.vehicleId != IEntityAttachOutgoingPacket.DISMOUNT_VEHICLE_ID;
	}
	
	public Boolean isCurrentEntity(int entityId)
	{
		if (this.entityId == null)
		{
			return null;
		}

		return this.entityId.equals(entityId);
	}
	
	public Boolean isSprinting()
	{
		if (this.sprintStatus == null)
		{
			return null;
		}
		
		return this.sprintStatus.sprinting;
	}
	
	public Boolean wasSprinting()
	{
		if (this.sprintStatus == null)
		{
			return null;
		}
		
		return this.sprintStatus.wasSprinting;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends GameStatusAwareSubmodule> T getSubmodule(Class<? extends T> clazz)
	{
		return (T)this.submodules.get(clazz);
	}
	
	private static enum SprintStatus
	{
		NOT_SPRINTING(false, false),
		START_SPRINTING(true, false),
		STOP_SPRINTING(false, true),
		SPRINTING(true, true);
		
		private final boolean sprinting;
		private final boolean wasSprinting;
		
		private SprintStatus(boolean sprinting, boolean wasSprinting)
		{
			this.sprinting = sprinting;
			this.wasSprinting = wasSprinting;
		}
	}
}
