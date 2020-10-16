package net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;

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
