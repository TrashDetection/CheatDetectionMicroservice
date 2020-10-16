package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.game.item.IItemStack;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class BlockPlaceNoSwingViolation extends UserViolation
{
	private final IItemStack item;
	
	public BlockPlaceNoSwingViolation(IItemStack item)
	{
		super(ViolationType.CRITICAL);
		
		this.item = item;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}

	@Override
	public String toString()
	{
		return String.format("Block place no swing | Item: %s", this.item);
	}
}
