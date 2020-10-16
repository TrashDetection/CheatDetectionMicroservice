package net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.utils.ByteBufUtils;

@RequiredArgsConstructor
public class ServerRegisteredOutgoingPacket implements OutgoingPacket
{
	@Getter private final UUID uniqueId;
	
	@Override
	public void write(ByteBuf out)
	{
		ByteBufUtils.writeUniqueId(out, this.uniqueId);
	}
}
