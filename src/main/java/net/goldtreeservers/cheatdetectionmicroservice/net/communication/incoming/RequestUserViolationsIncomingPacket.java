package net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.IncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.UserViolationsOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import net.goldtreeservers.cheatdetectionmicroservice.user.BaseUser;

public class RequestUserViolationsIncomingPacket implements IncomingPacket
{
	private int sessionId;
	
	@Override
	public void read(ByteBuf in)
	{
		this.sessionId = in.readInt();
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx) throws Exception
	{
		if (handler.getServerConnection() == null)
		{
			return;
		}
		
		BaseUser user = handler.getServerConnection().getUser(this.sessionId);
		if (user != null)
		{
			ctx.writeAndFlush(new UserViolationsOutgoingPacket(this.sessionId));
		}
		else
		{
			ctx.writeAndFlush(new UserViolationsOutgoingPacket(this.sessionId));
		}
	}
}
