package fi.joniaromaa.cheatdetectionmicroservice.net.handler;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.NetworkManager;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.DisconnectOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing.PingOutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConfig;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.Setter;

public class ServerConnectionHandler extends SimpleChannelInboundHandler<IncomingPacket>
{
	@Getter private final NetworkManager networkManager;
	
	@Getter private Channel channel;
	
	@Getter @Setter private ServerConfig config;
	@Getter @Setter private ServerConnection serverConnection;
	
	public ServerConnectionHandler(NetworkManager networkManager)
	{
		this.networkManager = networkManager;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		this.channel = ctx.channel();
		
		ctx.fireChannelActive();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket msg) throws Exception
	{
		msg.handle(this, ctx);
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object e)
	{
		if (e instanceof IdleStateEvent)
		{
			this.channel.writeAndFlush(new PingOutgoingPacket());
		}
		
		ctx.fireUserEventTriggered(e);
	}
	
	public void disconnect(String reason)
	{
		this.channel.config().setAutoRead(false);
		
		this.channel.writeAndFlush(new DisconnectOutgoingPacket(reason)).addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future) throws Exception
			{
				future.channel().close();
			}
		});
	}
}
