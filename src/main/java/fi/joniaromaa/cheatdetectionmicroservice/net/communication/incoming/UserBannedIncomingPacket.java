package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
