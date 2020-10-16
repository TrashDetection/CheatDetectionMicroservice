package fi.joniaromaa.cheatdetectionmicroservice.net.handler;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.MicroservicePacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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
