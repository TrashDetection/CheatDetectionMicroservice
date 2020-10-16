package fi.joniaromaa.cheatdetectionmicroservice.net;

import java.util.concurrent.TimeUnit;

import fi.joniaromaa.cheatdetectionmicroservice.net.communication.MicroservicePacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ExceptionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.PacketDecoderHandler;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.PacketEncoderHandler;
import fi.joniaromaa.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnectionManager;
import fi.joniaromaa.cheatdetectionmicroservice.utils.NettyUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;

public class NetworkManager
{
	@Getter private final ServerConnectionManager serverConnectionManager;
	
	@Getter private MicroservicePacketManager packetManager;
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup childGroup;
	
	public NetworkManager(ServerConnectionManager serverConnectionManager)
	{
		this.serverConnectionManager = serverConnectionManager;
		
		this.packetManager = new MicroservicePacketManager();
		
		this.bossGroup = NettyUtils.createEventLoopGroup(1);
		this.childGroup = NettyUtils.createEventLoopGroup();
	}
	
	public void start()
	{
		ServerBootstrap boostrap = new ServerBootstrap();
		boostrap.group(this.bossGroup, this.childGroup)
			.channel(NettyUtils.getServerChannel())
			.option(ChannelOption.SO_BACKLOG, 1000)
			.childOption(ChannelOption.TCP_NODELAY, true)
			.childHandler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel(SocketChannel channel) throws Exception
				{
					ChannelPipeline pipeline = channel.pipeline();

					pipeline.addLast(new LengthFieldPrepender(3));
					pipeline.addLast(new PacketEncoderHandler(NetworkManager.this.packetManager));

					pipeline.addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
					pipeline.addLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS));
					
					pipeline.addLast(new LengthFieldBasedFrameDecoder(1 << 24, 0, 3, 0, 3));
					pipeline.addLast(new PacketDecoderHandler(NetworkManager.this.packetManager));
					pipeline.addLast(new ServerConnectionHandler(NetworkManager.this));
					pipeline.addLast(new ExceptionHandler());
				}
			});

		boostrap.bind("0.0.0.0", 5555).syncUninterruptibly();
	}
	
	public void stop()
	{
		this.bossGroup.shutdownGracefully();
		this.childGroup.shutdownGracefully();
	}
}
