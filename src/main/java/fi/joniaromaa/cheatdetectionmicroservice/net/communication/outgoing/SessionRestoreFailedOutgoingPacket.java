package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import io.netty.buffer.ByteBuf;

public class SessionRestoreFailedOutgoingPacket implements OutgoingPacket
{
	@Override
	public void write(ByteBuf out)
	{
	}
}
