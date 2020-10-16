package net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing;

import io.netty.buffer.ByteBuf;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;

public class PingOutgoingPacket implements OutgoingPacket
{
	@Override
	public void write(ByteBuf out)
	{
		//Do nothing for now, might add id, but thats not really needed as the point isin't to test latency
	}
}
