package net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.IncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import net.goldtreeservers.cheatdetectionmicroservice.user.ServerUser;
import net.goldtreeservers.cheatdetectionmicroservice.utils.ByteBufUtils;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.StreamingXXHash64;
import net.jpountz.xxhash.XXHashFactory;

public class UserDataIncomingPacket implements IncomingPacket
{
	private static final Logger LOGGER = LogManager.getLogger(UserDataIncomingPacket.class);
	
	private static final LZ4Factory lz4Factory = LZ4Factory.fastestInstance();
	private static final XXHashFactory xxHashFactory = XXHashFactory.fastestInstance();
	
	@Getter private long incomingHash;
	@Getter private long outgoingHash;
	
	@Getter private int originalSize;
	@Getter private ByteBuf compressedData;
	
	@Override
	public void read(ByteBuf in)
	{
		this.incomingHash = in.readLong();
		this.outgoingHash = in.readLong();
		
		this.originalSize = ByteBufUtils.readVarInt(in);
		int compressedSize = ByteBufUtils.readVarInt(in);
		
		this.compressedData = PooledByteBufAllocator.DEFAULT.ioBuffer(compressedSize);
		
		in.readBytes(this.compressedData);
	}

	@Override
	public void handle(ServerConnectionHandler handler, ChannelHandlerContext ctx) throws Exception
	{
		if (handler.getConfig() == null || handler.getServerConnection() == null)
		{
			this.compressedData.release();
			
			return;
		}

		ByteBuf buf = PooledByteBufAllocator.DEFAULT.ioBuffer(this.originalSize);
		
		try
		{
			try
			{
				UserDataIncomingPacket.lz4Factory.fastDecompressor().decompress(this.compressedData.nioBuffer(0, this.compressedData.readableBytes()), buf.nioBuffer(0, this.originalSize));
			}
			finally
			{
				this.compressedData.release();
			}

			buf.writerIndex(this.originalSize);
			
			int sessionId = ByteBufUtils.readVarInt(buf);
			int userId = ByteBufUtils.readVarInt(buf);
			int protocolVersion = ByteBufUtils.readVarInt(buf);
			
			int version = ByteBufUtils.readVarInt(buf);
			
			boolean incomplete = buf.readBoolean();

			ServerUser user = handler.getServerConnection().newUserData(sessionId, userId, protocolVersion, version, incomplete);
			if (user == null)
			{
				return;
			}
			
			//Extra stuff
			long startedOn = buf.readLong();
			long endedOn = buf.readLong();
			
			try
			{
				ByteBuf incoming = Unpooled.copiedBuffer(buf.readSlice(ByteBufUtils.readVarInt(buf)));

				long incomingHash = this.createHash(userId, sessionId, incoming);
				if (incomingHash != this.incomingHash)
				{
					throw new RuntimeException(String.format("User data incoming bytes hash was different! Expected: %d but was %d!", incomingHash, this.incomingHash));
				}
				
				ByteBuf outgoing = Unpooled.copiedBuffer(buf.readSlice(ByteBufUtils.readVarInt(buf)));
				
				long outgoingHash = this.createHash(userId, sessionId, outgoing);
				if (outgoingHash != this.outgoingHash)
				{
					throw new RuntimeException(String.format("User data outgoing bytes hash was different! Expected: %d but was %d!", outgoingHash, this.outgoingHash));
				}
				
				long time = endedOn - startedOn;
				
				user.addBytesToAnalyze(version, incomingHash, outgoingHash, incoming, outgoing, time);

				if (buf.isReadable())
				{
					throw new RuntimeException("Readable bytes after user data: " + buf.readableBytes());
				}
				else
				{
					UserDataIncomingPacket.LOGGER.info("Got user: " + handler.getConfig().getServerId() + " | " + userId + " | " + sessionId);
				}
			}
			catch(Throwable e)
			{
				UserDataIncomingPacket.LOGGER.fatal(e);
				
				handler.getServerConnection().removeUser(sessionId);
			}
		}
		finally
		{
			buf.release();
		}
	}
	
	private long createHash(long userId, long sessionId, ByteBuf buf)
	{
		long baseSeed = userId | sessionId << 32;
		
		StreamingXXHash64 hash = this.getHashing(baseSeed);
		
		if (buf.hasArray())
		{
			hash.update(buf.array(), buf.arrayOffset(), buf.readableBytes());
		}
		else
		{
			byte[] buffer = new byte[1024 * 8];
			while (buf.isReadable())
			{
				int written = Math.min(buffer.length, buf.readableBytes());
				
				buf.readBytes(buffer);

				hash.update(buffer, 0, written);
			}
		}
		
		return hash.getValue();
	}
	
	private StreamingXXHash64 getHashing(long seed)
	{
		return UserDataIncomingPacket.xxHashFactory.newStreamingHash64(seed);
	}
}
