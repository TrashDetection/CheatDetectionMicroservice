package fi.joniaromaa.cheatdetectionmicroservice.utils;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUtils
{
	private static final boolean epoll = Epoll.isAvailable();
	
	static
	{
		
	}
	
	public static EventLoopGroup createEventLoopGroup()
	{
		return NettyUtils.createEventLoopGroup(0);
	}
	
	public static EventLoopGroup createEventLoopGroup(int threads)
	{
		return NettyUtils.epoll ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
	}
	
	public static Class<? extends ServerChannel> getServerChannel()
	{
		return NettyUtils.epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
	}
	
	public static Class<? extends SocketChannel> getSocketChannel()
	{
		return NettyUtils.epoll ? EpollSocketChannel.class : NioSocketChannel.class;
	}
}
