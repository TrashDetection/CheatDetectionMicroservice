package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import java.util.UUID;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
