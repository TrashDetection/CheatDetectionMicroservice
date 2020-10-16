package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.ServerRegisteredOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class RegisterServerIncomingPacket implements IncomingPacket
{
	@Override
	public void read(ByteBuf in)
	{
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx)
	{
		if (handler.getConfig() == null)
		{
			handler.disconnect("You need to be logged in before registering server connection");
			return;
		}
		
		if (handler.getServerConnection() != null)
		{
			handler.disconnect("Unexpected packet, you already have active server conenction");
			return;
		}
		
		ServerConnection connection = handler.getNetworkManager().getServerConnectionManager().registerConnection(handler);
		if (connection != null)
		{
			handler.setServerConnection(connection);

			ctx.writeAndFlush(new ServerRegisteredOutgoingPacket(connection.getUniqueId()));
		}
		else
		{
			handler.disconnect("Failed to register connection");
		}
	}
}
