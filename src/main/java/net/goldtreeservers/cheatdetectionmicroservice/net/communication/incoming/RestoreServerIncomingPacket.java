package net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.IncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.ServerRegisteredOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.SessionRestoreFailedOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConnection;
import net.goldtreeservers.cheatdetectionmicroservice.utils.ByteBufUtils;

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
