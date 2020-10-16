package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.inventory;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IClickWindowIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;

public class InvalidContainerTransactionIdViolation extends UserViolation
{
	private final byte windowId;
	private final short slot;
	private final byte button;
	private final short actionNumberOld;
	private final short actionNumberNew;
	private final IClickWindowIncomingPacket.Mode mode;
	
	private final int openInventory;
	
	public InvalidContainerTransactionIdViolation(byte windowId, short slot, byte button, short actionNumberOld, short actionNumberNew, IClickWindowIncomingPacket.Mode mode, int openInventory)
	{
		super(ViolationType.CRITICAL);
		
		this.windowId = windowId;
		this.slot = slot;
		this.button = button;
		this.actionNumberOld = actionNumberOld;
		this.actionNumberNew = actionNumberNew;
		this.mode = mode;
		
		this.openInventory = openInventory;
	}

	@Override
	public int getViolationPoints()
	{
		return 0;
	}

	@Override
	public String toString()
	{
		return String.format("Invalid container transaction id | Window Id: %d | Slot: %d | Button: %d | Action number old: %d | Action number new: %d | Mode: %s | Open Inventory: %d", this.windowId, this.slot, this.button, this.actionNumberOld, this.actionNumberNew, this.mode, this.openInventory);
	}
}
