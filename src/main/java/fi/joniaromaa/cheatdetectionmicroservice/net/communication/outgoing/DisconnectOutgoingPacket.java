package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
