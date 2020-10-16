package fi.joniaromaa.cheatdetectionmicroservice.utils;

import java.io.IOException;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;

public class OutputStreamUtils
{
    public static void chunkedByteArrayToStream(OutputStream stream, ChunkedByteArray array) throws IOException
    {
    	long left = array.size64();
		for(byte[] element : array.array())
		{
			if (element == null)
			{
				break;
			}

			stream.write(element, 0, (int)Math.min(ChunkedByteArray.CHUNK_SIZE, left));
			
			left -= ChunkedByteArray.CHUNK_SIZE;
		}
    }
    
    public static void byteBufToOutputStream(OutputStream stream, ByteBuf buf) throws IOException
    {
		buf.readBytes(stream, buf.readableBytes());
		
		buf.resetReaderIndex();
    }
}
