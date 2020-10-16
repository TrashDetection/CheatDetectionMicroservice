package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BanUserOutgoingPacket implements OutgoingPacket
{
	private final int banId;
	private final int userId;
	
	@Override
	public void write(ByteBuf out)
	{
		out.writeInt(this.banId);
		out.writeInt(this.userId);
	}
}
