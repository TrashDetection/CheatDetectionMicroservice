package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PongIncomingPacket implements IncomingPacket 
{
	@Override
	public void read(ByteBuf in)
	{
		//Nothing, empty packet, response to @PingOutgoingPacket
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx)
	{
		handler.getNetworkManager().getServerConnectionManager().serverPing(handler);
	}
}
