package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.world;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.game.item.IItemStack;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IAnimationIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IBlockPlaceIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world.BlockPlaceBlockPlaceViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world.BlockPlaceNoSwingViolation;

public class BlockPlaceNoSwingModule extends Module
{
	private boolean waitingForSwing;
	private IItemStack lastItem;

	public BlockPlaceNoSwingModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addIncomingHandlerPre(IBlockPlaceIncomingPacket.class, this::handleBlockPlace);
		this.addIncomingHandlerPre(IAnimationIncomingPacket.class, this::handleAnimation);
	}

	@Override
	public boolean pre()
	{
		return super.pre() && this.getEvaluation().getUser().getProtocolVersion() == 47;
	}
	
	@Override
	public boolean handlesOutgoingPackets()
	{
		return false;
	}

	@Override
	public boolean handlesIncomingPackets()
	{
		return true;
	}
	
	private void handleBlockPlace(IBlockPlaceIncomingPacket packet)
	{
		IBlockPlaceIncomingPacket blockPlace = (IBlockPlaceIncomingPacket)packet;
		if (!blockPlace.isInteract())
		{
			if (!this.waitingForSwing)
			{
				this.lastItem = blockPlace.getItem();
				if (this.lastItem != null && !this.lastItem.isAir())
				{
					//After this packet there should be swing arm packet or another block place... for intereact....
					
					this.waitingForSwing = true;
					
					return;
				}
			}
			else
			{
				//Block place after block place, thats not right

				this.addViolation(new BlockPlaceBlockPlaceViolation(blockPlace.getItem(), this.lastItem));
			}
		}
		else
		{
			//Interact is the last one in the right click handling, there is no other packets that could be sent
			//Also swing won't happen on interact
			//This is only sent if the block place failed or was not executed for some reason (can't place block) but only if the hand is not empty
			//Forge mod might cancel this and cause a lot trouble to us but hopefully there aren't mods that interfier with vanilla stuff
			
			//Also do nothing, just run the end logic
		}
		
		this.waitingForSwing = false;
		this.lastItem = null;
	}
	
	private void handleAnimation(IAnimationIncomingPacket packet)
	{
		this.waitingForSwing = false;
		this.lastItem = null;
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (super.fireIncomingPacketHandler(packet))
		{
			return;
		}
	
		if (this.waitingForSwing)
		{
			this.addViolation(new BlockPlaceNoSwingViolation(this.lastItem));
		}
		
		this.waitingForSwing = false;
	}
}
