package net.goldtreeservers.cheatdetectionmicroservice.common.communication;

import io.netty.buffer.ByteBuf;

public interface OutgoingPacket
{
	public void write(ByteBuf out);
	
	public default void read(ByteBuf in)
	{
		throw new UnsupportedOperationException();
	}
	
	public default boolean isWritable()
	{
		return true;
	}

	public default boolean isReadable()
	{
		return false;
	}
}
