package net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.IncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;

public class DisconnectIncomingPacket implements IncomingPacket
{
	@Override
	public void read(ByteBuf in)
	{
		
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx)
	{
		handler.getNetworkManager().getServerConnectionManager().unregisterConnection(handler);
	}
}