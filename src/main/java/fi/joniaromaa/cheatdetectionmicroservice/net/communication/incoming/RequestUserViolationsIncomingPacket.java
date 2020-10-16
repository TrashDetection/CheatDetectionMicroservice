package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.UserViolationsOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.user.BaseUser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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
