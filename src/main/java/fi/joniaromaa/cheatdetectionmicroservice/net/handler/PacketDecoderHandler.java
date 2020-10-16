package fi.joniaromaa.cheatdetectionmicroservice.net.handler;

import java.util.List;

import fi.joniaromaa.cheatdetectionmicroservice.net.communication.MicroservicePacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketDecoderHandler extends ByteToMessageDecoder
{
	private MicroservicePacketManager packetManager;
	
	public PacketDecoderHandler(MicroservicePacketManager packetManager)
	{
		this.packetManager = packetManager;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
	{
		out.add(this.packetManager.readIncomingPacket(in));
	}
}
