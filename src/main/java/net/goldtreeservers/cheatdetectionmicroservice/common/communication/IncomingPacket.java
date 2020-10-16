package net.goldtreeservers.cheatdetectionmicroservice.common.communication;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;

public interface IncomingPacket
{
	public void read(ByteBuf in);
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx) throws Exception;
	
	public default void write(ByteBuf out)
	{
		throw new UnsupportedOperationException();
	}

	public default boolean isReadable()
	{
		return true;
	}

	public default boolean isHandlable()
	{
		return true;
	}
	
	public default boolean isWritable()
	{
		return false;
	}
}
