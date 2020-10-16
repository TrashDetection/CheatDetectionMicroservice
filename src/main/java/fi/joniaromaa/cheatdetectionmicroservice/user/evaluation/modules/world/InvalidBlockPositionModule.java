package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.Module;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world.InvalidInteractFacingViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world.InvalidInteractPositionViolation;
import fi.joniaromaa.minecrafthook.common.network.incoming.IBlockPlaceIncomingPacket;
import fi.joniaromaa.minecrafthook.common.utils.IBlockPos;

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
