package net.goldtreeservers.cheatdetectionmicroservice.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public abstract class ChunkedByteArrayBufferReader<T>
{
	private ChunkedObjectArray<T> packets;
	private int lastPosition;
	
	public ChunkedByteArrayBufferReader()
	{
		this.packets = new ChunkedObjectArray<>();
	}
	
	public void read(ChunkedByteArray array)
	{
		int size = Math.toIntExact(array.size64() - this.lastPosition);
		
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(size, size);
		
		try
		{
			ByteBufUtils.chunkedByteArrayToByteBuf(buf, array, this.lastPosition);
			
			this.read0(buf);
			
			this.lastPosition += size;
		}
		finally
		{
			buf.release();
		}
	}
	
	protected abstract void read0(ByteBuf buffer);
	
	protected void addPacket(T packet)
	{
		this.packets.add(packet);
	}
	
	public ChunkedObjectArray<T> getPacketsDangerous()
	{
		return this.packets;
	}
}
