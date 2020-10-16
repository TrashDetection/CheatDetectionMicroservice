package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.BlockPlaceButFacingEntityViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.JustStoppedBlockingViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.inventory.PlayerAttackAfterInteractViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.OnBlockingKeptSprintViolation;
import fi.joniaromaa.minecrafthook.common.game.item.IItemStack;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.*;
import fi.joniaromaa.minecrafthook.common.network.outgoing.ICameraOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IUpdateHealthOutgoingPacket;

public class PlayerInterackStatusModule extends GameStatusAwareSubmodule
{
	private Boolean state;
	
	private int foodLevel;
	
	private Boolean sprinting;
	
	private Boolean blocking;
	private boolean justStoppedBlocking;
	
	public PlayerInterackStatusModule(GameStatusAwareModule module, UserEvaluation evaluation)
	{
		super(module, evaluation);
		
		this.resetState();
		
		this.addRequireConfirmation(IJoinOutgoingPacket.class);
		this.addRequireConfirmation(IRespawnOutgoingPacket.class);
		this.addRequireConfirmation(ICameraOutgoingPacket.class);
		this.addRequireConfirmation(IUpdateHealthOutgoingPacket.class);
	}
	
	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}

	@Override
	public void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IJoinOutgoingPacket
				|| packet instanceof IRespawnOutgoingPacket)
		{
			this.resetState();
		}
		else if (packet instanceof ICameraOutgoingPacket)
		{
			if (!this.getModule().isCurrentRenderingEntity())
			{
				this.resetState();
			}
		}
		else if (packet instanceof IUpdateHealthOutgoingPacket)
		{
			IUpdateHealthOutgoingPacket updateHealth = (IUpdateHealthOutgoingPacket)packet;
			
			this.foodLevel = updateHealth.getFood();
		}
	}
	
	private void resetState()
	{
		this.state = null;
		
		this.foodLevel = 20;
		
		this.sprinting = null;
		
		this.blocking = null;
		this.justStoppedBlocking = false;
	}

	@Override
	public void playerRemovedFromChunk()
	{
		this.resetState();
	}
	
	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (Boolean.FALSE.equals(this.getModule().isSpectator()) && this.getModule().isCurrentRenderingEntity())
		{
			if (packet instanceof IHeldItemChangeIncomingPacket)
			{
				this.justStoppedBlocking = false;
			}
			else if (packet instanceof IUseEntityIncomingPacket)
			{
				if (this.justStoppedBlocking)
				{
					this.addViolation(new JustStoppedBlockingViolation());
				}
				
				if (Boolean.TRUE.equals(this.blocking))
				{
					//This requires proper tracking for inventory events
					//violations = ListUtils.unmodifiableListAdd(violations, new PlayerBlockingInteractViolation());
				}

				IUseEntityIncomingPacket useEntityPacket = (IUseEntityIncomingPacket)packet;
				if (useEntityPacket.getAction() == IUseEntityIncomingPacket.Action.ATTACK)
				{
					if (this.getModule().isInGame())
					{
						//There can not be attack packets after there has been interact ones
						if (Boolean.TRUE.equals(this.state))
						{
							this.addViolation(new PlayerAttackAfterInteractViolation());
							
							return;
						}
					}
					
					this.state = false;
				}
				else if (useEntityPacket.getAction() == IUseEntityIncomingPacket.Action.INTERACT || useEntityPacket.getAction() == IUseEntityIncomingPacket.Action.INTERACT_AT)
				{
					this.state = true;
				}
			}
			else if (packet instanceof IBlockPlaceIncomingPacket)
			{
				if (this.justStoppedBlocking)
				{
					this.addViolation(new JustStoppedBlockingViolation());
				}
				
				if (Boolean.TRUE.equals(this.blocking))
				{
					//This requires proper tracking for inventory events
					//violations = ListUtils.unmodifiableListAdd(violations, new PlayerBlockingInteractViolation());
				}

				if (this.getModule().isInGame())
				{
					IBlockPlaceIncomingPacket blockPlace = (IBlockPlaceIncomingPacket)packet;
					if (!blockPlace.isInteract())
					{
						if (this.state != null)
						{
							this.addViolation(new BlockPlaceButFacingEntityViolation());
							
							return;
						}
					}
					else
					{
						IItemStack item = blockPlace.getItem();
						if (item != null && !item.isAir())
						{
							switch(item.getItemId())
							{
								case 268: //Wooden sword
								case 283: //Golden sword
								case 272: //Stone sword
								case 267: //Iron sword
								case 276: //Diamond sword
								case 335: //Milk bucket
								{
									this.blocking = false; //The above will always block
								}
								break;
								case 261: //Bow
								{
									//Players in creative will always have arrows, on survival we need inventory checks
									if (Boolean.TRUE.equals(this.getModule().inCreative()))
									{
										this.blocking = false;
									}
								}
								break;
								case 322: //Golden apple
								{
									//You can't eat golden apples in "invulnerable" mode
									if (Boolean.FALSE.equals(this.getModule().getInvulnerable()))
									{
										this.blocking = false;
									}
								}
								break;
								case 373: //Potion
								{
									//If its splash potion, ignore
									if ((item.getMetadata() & 0x4000) == 0)
									{
										this.blocking = false;
									}
								}
								break;
								//Too complicated due to client being dump
								/*case 260: //Apple
								case 282: //Stew
								case 297: //Bread
								case 319: //Raw pork
								case 320: //Cooked pork
								case 349: //Raw fish
								case 350: //Cooked fish
								case 357: //Cookie
								case 360: //Melon
								case 363: //Raw beef
								case 364: //Steak
								case 365: //Raw chicken
								case 366: //Cooked chicken
								case 367: //Rotten flesh
								case 375: //Spider eye
								case 391: //Carrot
								case 392: //Potato
								case 393: //Baked potato
								case 394: //Poisonous potato
								case 396: //Golden carrot
								case 400: //Pumpkin pie
								case 411: //Raw rabbit
								case 412: //Cooked rabbit
								case 413: //Rabbit stew
								case 423: //Raw mutton
								case 424: //Cooked mutton
								{
									if (this.needFood())
									{
										this.blocking = false;
									}
								}
								break;*/
							}
						}
					}
				}
			}
			else if (packet instanceof IPlayerDiggingIncomingPacket)
			{
				if (this.getModule().isInGame())
				{
					IPlayerDiggingIncomingPacket diggingPacket = (IPlayerDiggingIncomingPacket)packet;
					if (diggingPacket.getAction() == IPlayerDiggingIncomingPacket.Action.RELEASE_USE_ITEM)
					{
						this.blocking = null;
						this.justStoppedBlocking = true;
					}
				}
			}
		}
		
		if (packet instanceof IPlayerIncomingPacket)
		{
			if (this.getModule().isJustTeleported())
			{
				return;
			}
			
			this.state = null;
			
			if (Boolean.FALSE.equals(this.blocking))
			{
				this.blocking = true;
				
				if (!this.getModule().inVehicle()) //You can sprint and block inside vehicle
				{
					if (Boolean.TRUE.equals(this.sprinting))
					{
						this.addViolation(new OnBlockingKeptSprintViolation());
					}
				}
			}
			
			this.justStoppedBlocking = false;
		}
		else if (packet instanceof IEntityActionIncomingPacket)
		{
			IEntityActionIncomingPacket entityAction = (IEntityActionIncomingPacket)packet;
			if (entityAction.getAction() == IEntityActionIncomingPacket.Action.START_SPRINTING)
			{
				this.sprinting = true;
			}
			else if (entityAction.getAction() == IEntityActionIncomingPacket.Action.STOP_SPRINTING)
			{
				this.sprinting = false;
			}
		}
	}
	
	public boolean needFood()
	{
		return this.foodLevel < 20;
	}
}
