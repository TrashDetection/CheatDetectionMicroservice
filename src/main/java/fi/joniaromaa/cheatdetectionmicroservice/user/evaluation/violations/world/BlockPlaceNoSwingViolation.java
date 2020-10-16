package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;
import fi.joniaromaa.minecrafthook.common.game.item.IItemStack;

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
