package net.goldtreeservers.cheatdetectionmicroservice.utils;

public class ChunkedObjectArray<T>
{
	public static final int CHUNK_SHIFT = 16;
	public static final int CHUNK_SIZE = 1 << ChunkedObjectArray.CHUNK_SHIFT;
	public static final int CHUNK_MASK = ChunkedObjectArray.CHUNK_SIZE - 1;
	
	private static final int INITIAL_CHUNK_CAPACITY = 8;
	
	private T[][] chunks;
	private int chunksCount;
	
	private long pointer;
	
	@SuppressWarnings("unchecked")
	public ChunkedObjectArray()
	{
		this.chunks = (T[][])new Object[ChunkedObjectArray.INITIAL_CHUNK_CAPACITY][];
	}
	
	private ChunkedObjectArray(ChunkedObjectArray<T> copy)
	{
		this.chunks = copy.chunks;
		this.chunksCount = copy.chunksCount;
		
		this.pointer = copy.pointer;
	}
	
	@SuppressWarnings("unchecked")
	public void ensureCapacity(long amount)
	{
		int chunksNeeded = (int)((this.pointer + amount) >>> ChunkedObjectArray.CHUNK_SHIFT);
		if (chunksNeeded >= this.chunks.length)
		{
			T[][] newChunks = (T[][])new Object[Math.max(this.chunks.length << 1, chunksNeeded << 1)][];
			
			System.arraycopy(this.chunks, 0, newChunks, 0, this.chunksCount);
			
			this.chunks = newChunks;
		}
		
		while (chunksNeeded >= this.chunksCount)
		{
			this.chunks[this.chunksCount++] = (T[])new Object[ChunkedObjectArray.CHUNK_SIZE];
		}
	}
	
	public void add(T value)
	{
		this.ensureCapacity(1);
		
		int currentChunk = (int)(this.pointer >>> ChunkedObjectArray.CHUNK_SHIFT);
		int index = (int)(this.pointer & ChunkedObjectArray.CHUNK_MASK);

		this.chunks[currentChunk][index] = value;
		
		this.pointer++;
	}
	
	public T get(long index)
	{
		return this.chunks[(int)(index >>> ChunkedObjectArray.CHUNK_SHIFT)][(int)(index & ChunkedObjectArray.CHUNK_MASK)];
	}
	
	public long size64()
	{
		return this.pointer;
	}
	
	public T[][] array()
	{
		return this.chunks;
	}
	
	public ChunkedObjectArray<T> getDangerousSoftCopy()
	{
		return new ChunkedObjectArray<T>(this);
	}
	
    public boolean forEachValue(Procedure<? super T> procedure)
    {
    	long left = this.pointer;
    	
    	for(int i = 0; i < this.chunksCount; i++)
    	{
    		T[] element = this.chunks[i];
    		
    		for(int j = 0; j < Math.min(ChunkedObjectArray.CHUNK_SIZE, left); j++)
    		{
    			procedure.execute(element[j]);
    		}
    		
    		left -= ChunkedObjectArray.CHUNK_SIZE;
    	}
    	
        return true;
    }
	
	public static interface Procedure<T>
	{
	    public void execute(T object);
	}
}
