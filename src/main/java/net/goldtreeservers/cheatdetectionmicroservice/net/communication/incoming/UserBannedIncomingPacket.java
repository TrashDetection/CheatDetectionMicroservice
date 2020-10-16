package net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.IncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;

public class UserBannedIncomingPacket implements IncomingPacket
{
	private int banId;
	
	@Override
	public void read(ByteBuf in)
	{
		this.banId = in.readInt();
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx) throws Exception
	{
		if (handler.getConfig() == null ||  handler.getServerConnection() == null)
		{
			return;
		}
		
		handler.getServerConnection().getManager().getBanManager().markBanResolvedServer(this.banId, handler.getConfig().getServerId());
	}
}
