package net.goldtreeservers.cheatdetectionmicroservice.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.MicroservicePacketManager;

public class PacketEncoderHandler extends MessageToByteEncoder<OutgoingPacket>
{
	private MicroservicePacketManager packetManager;
	
	public PacketEncoderHandler(MicroservicePacketManager packetManager)
	{
		this.packetManager = packetManager;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, OutgoingPacket msg, ByteBuf out) throws Exception
	{
		this.packetManager.writeOutgoingPacket(msg, out);
	}
}
