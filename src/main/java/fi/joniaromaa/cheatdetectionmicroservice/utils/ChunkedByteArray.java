package fi.joniaromaa.cheatdetectionmicroservice.utils;

import io.netty.buffer.ByteBuf;

public class ChunkedByteArray
{
	public static final int CHUNK_SHIFT = 16;
	public static final int CHUNK_SIZE = 1 << ChunkedByteArray.CHUNK_SHIFT;
	public static final int CHUNK_MASK = ChunkedByteArray.CHUNK_SIZE - 1;
	
	private static final int INITIAL_CHUNK_CAPACITY = 8;
	
	private byte[][] chunks;
	private int chunksCount;
	
	private long pointer;
	
	public ChunkedByteArray()
	{
		this.chunks = new byte[ChunkedByteArray.INITIAL_CHUNK_CAPACITY][];
	}
	
	public void ensureCapacity(long amount)
	{
		int chunksNeeded = (int)((this.pointer + amount) >>> ChunkedByteArray.CHUNK_SHIFT);
		if (chunksNeeded >= this.chunks.length)
		{
			byte[][] newChunks = new byte[Math.max(this.chunks.length << 1, chunksNeeded << 1)][];
			
			System.arraycopy(this.chunks, 0, newChunks, 0, this.chunksCount);
			
			this.chunks = newChunks;
		}
		
		while (chunksNeeded >= this.chunksCount)
		{
			this.chunks[this.chunksCount++] = new byte[ChunkedByteArray.CHUNK_SIZE];
		}
	}
	
	public void write(byte value)
	{
		this.ensureCapacity(1);
		
		int currentChunk = (int)(this.pointer >>> ChunkedByteArray.CHUNK_SHIFT);
		int index = (int)(this.pointer & ChunkedByteArray.CHUNK_MASK);

		this.chunks[currentChunk][index] = value;
		
		this.pointer++;
	}
	
	public void write(ByteBuf buffer)
	{
		this.ensureCapacity(buffer.readableBytes());
		
		while (buffer.isReadable())
		{
			int currentChunk = (int)(this.pointer >>> ChunkedByteArray.CHUNK_SHIFT);
			int index = (int)(this.pointer & ChunkedByteArray.CHUNK_MASK);
			int length = Math.min(ChunkedByteArray.CHUNK_SIZE - index, buffer.readableBytes());
		
			buffer.readBytes(this.chunks[currentChunk], index, length);
			
			this.pointer += length;
		}
	}
	
	public long size64()
	{
		return this.pointer;
	}
	
	public byte[][] array()
	{
		return this.chunks;
	}
}
