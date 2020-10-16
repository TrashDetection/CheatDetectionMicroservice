package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import java.util.UUID;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.ServerRegisteredOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.SessionRestoreFailedOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnection;
import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

public class RestoreServerIncomingPacket implements IncomingPacket
{
	@Getter private UUID sessionId;
	
	@Override
	public void read(ByteBuf in)
	{
		this.sessionId = ByteBufUtils.readUniqueId(in);
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

		ServerConnection connection = handler.getNetworkManager().getServerConnectionManager().getConnetion(handler, this.sessionId);
		if (connection != null)
		{
			handler.setServerConnection(connection);
			
			connection.setHandler(handler);
			
			ctx.writeAndFlush(new ServerRegisteredOutgoingPacket(connection.getUniqueId()));
		}
		else
		{
			ctx.writeAndFlush(new SessionRestoreFailedOutgoingPacket());
		}
	}
}
