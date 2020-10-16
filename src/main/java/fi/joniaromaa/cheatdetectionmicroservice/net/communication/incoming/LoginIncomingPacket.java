package fi.joniaromaa.cheatdetectionmicroservice.net.communication.incoming;

import java.util.concurrent.TimeUnit;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.IncomingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConfig;
import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;

public class LoginIncomingPacket implements IncomingPacket
{
	@Getter private String username;
	@Getter private String password;
	
	@Override
	public void read(ByteBuf in)
	{
		this.username = ByteBufUtils.readString(in);
		this.password = ByteBufUtils.readString(in);
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx)
	{
		if (handler.getConfig() != null)
		{
			handler.disconnect("Unexcepted packet, you are already logged in");
			return;
		}

		if (!this.username.equals("isokissa3") && !this.password.equals("bestanticheatyeyewowowoomgisthisevenapasswordwtfisgoingon"))
		{
			handler.disconnect("Wait what");
			return;
		}
		
		handler.setConfig(new ServerConfig(0, 0));
		
		ChannelPipeline pipeline = ctx.channel().pipeline();
		
		pipeline.addFirst(new IdleStateHandler(3, 3, 0, TimeUnit.SECONDS)); //After they have logged in, start to try to keep the session open
	}
}
