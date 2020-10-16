package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IClickWindowIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class PlayerInventoryNotOpenViolation extends UserViolation
{
	private final short slot;
	private final byte button;
	private final short actionNumber;
	private final IClickWindowIncomingPacket.Mode mode;
	
	public PlayerInventoryNotOpenViolation(short slot, byte button, short actionNumber, IClickWindowIncomingPacket.Mode mode)
	{
		super(ViolationType.CRITICAL);
		
		this.slot = slot;
		this.button = button;
		this.actionNumber = actionNumber;
		this.mode = mode;
	}

	@Override
	public int getViolationPoints()
	{
		return 0; //Bad client
	}

	@Override
	public String toString()
	{
		return String.format("Player inventory not open | Slot: %d | Button: %d | Action number: %d | Mode: %s ", this.slot, this.button, this.actionNumber, this.mode);
	}
}
