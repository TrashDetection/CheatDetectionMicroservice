package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.world;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IBlockPlaceIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IBlockPos;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world.InvalidInteractFacingViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world.InvalidInteractPositionViolation;

public class InvalidBlockPositionModule extends Module
{
	public InvalidBlockPositionModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addIncomingHandlerPre(IBlockPlaceIncomingPacket.class, this::handleBlockPlace);
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
		IBlockPlaceIncomingPacket blockPlacePacket = (IBlockPlaceIncomingPacket)packet;
		if (blockPlacePacket.isInteract())
		{
			IBlockPos loc = blockPlacePacket.getPosition();
			if (loc.getX() != -1 && loc.getY() != -1 && loc.getZ() != -1)
			{
				this.addViolation(new InvalidInteractPositionViolation(loc));
			}
			else
			{
				if (blockPlacePacket.getFacingX() != 0.0F || blockPlacePacket.getFacingY() != 0.0F || blockPlacePacket.getFacingZ() != 0.0F)
				{
					this.addViolation(new InvalidInteractFacingViolation(blockPlacePacket.getFacingX(), blockPlacePacket.getFacingY(), blockPlacePacket.getFacingZ()));
				}
			}
		}
		//else if (blockPlacePacket.getPlacingDirection() < 0 || blockPlacePacket.getPlacingDirection() > 5)
		//{
		//	this.addViolation(new InvalidBlockPlaceDirectionViolation(blockPlacePacket.getPlacingDirection()));
		//}
	}
}
