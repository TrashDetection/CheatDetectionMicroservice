package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.world;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;
import fi.joniaromaa.minecrafthook.common.game.item.IItemStack;

public class BlockPlaceBlockPlaceViolation extends UserViolation
{
	private final IItemStack item;
	private final IItemStack lastItem;
	
	public BlockPlaceBlockPlaceViolation(IItemStack item, IItemStack lastItem)
	{
		super(ViolationType.CRITICAL);
		
		this.item = item;
		this.lastItem = lastItem;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}
	
	@Override
	public String toString()
	{
		return String.format("Block place block place violation | Item: %s | Last item: %s", this.item, this.lastItem);
	}
}
