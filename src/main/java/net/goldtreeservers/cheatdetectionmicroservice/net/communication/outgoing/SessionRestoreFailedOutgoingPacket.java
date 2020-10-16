package net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing;

import io.netty.buffer.ByteBuf;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;

public class SessionRestoreFailedOutgoingPacket implements OutgoingPacket
{
	@Override
	public void write(ByteBuf out)
	{
	}
}
