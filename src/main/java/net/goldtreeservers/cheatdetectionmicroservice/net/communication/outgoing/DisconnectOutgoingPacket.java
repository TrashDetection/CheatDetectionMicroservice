package net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.utils.ByteBufUtils;

@RequiredArgsConstructor
public class DisconnectOutgoingPacket implements OutgoingPacket
{
	@Getter private final String reason;
	
	@Override
	public void write(ByteBuf out)
	{
		ByteBufUtils.writeString(out, this.reason);
	}
}
