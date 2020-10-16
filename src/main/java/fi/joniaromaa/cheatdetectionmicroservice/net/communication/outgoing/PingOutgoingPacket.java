package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import io.netty.buffer.ByteBuf;

public class PingOutgoingPacket implements OutgoingPacket
{
	@Override
	public void write(ByteBuf out)
	{
		//Do nothing for now, might add id, but thats not really needed as the point isin't to test latency
	}
}
