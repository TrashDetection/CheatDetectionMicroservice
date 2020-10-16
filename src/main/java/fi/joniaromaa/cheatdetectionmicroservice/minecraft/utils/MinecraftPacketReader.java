package fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets.Procedure;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftPacket;
import fi.joniaromaa.minecrafthook.common.network.IPacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

public abstract class MinecraftPacketReader
{
	@Getter private final MinecraftPacketManager packetManager;
	
	private IPacketBuffer buffer;
	
	@Getter @Setter private ByteBuf backing;
	
	public MinecraftPacketReader(MinecraftPacketManager packetManager)
	{
		this(packetManager, packetManager.getHook().getNetworkHook().createEmptyBuffer());
	}
	
	public MinecraftPacketReader(MinecraftPacketManager packetManager, IPacketBuffer buffer)
	{
		this.packetManager = packetManager;
		
		this.buffer = buffer;
	}

	protected void read0(Procedure<? super IMinecraftPacket> procedure)
	{
		if (this.packetManager == null || this.backing == null)
		{
			return;
		}
		
		ByteBuf buffer = Unpooled.wrappedBuffer(this.buffer.getBuffer(), this.backing.retain());
		
		while (buffer.isReadable())
		{
			buffer.markReaderIndex();
			
			int length;
			try
			{
				length = ByteBufUtils.readVarInt(buffer);
				
				if (!buffer.isReadable(length))
				{
					buffer.resetReaderIndex();
					
					break;
				}
				
			}
			catch(IndexOutOfBoundsException e)
			{
				buffer.resetReaderIndex();
				
				break;
			}

			this.buffer.setBuffer(buffer.readRetainedSlice(length));

			try
			{
				IMinecraftPacket packet = this.read(this.buffer);
				if (packet != null)
				{
					procedure.execute(packet);
				}
			}
			finally
			{
				this.buffer.getBuffer().release();
			}
		}
		
		//Left over, save for later
		this.buffer.setBuffer(Unpooled.copiedBuffer(buffer));
		
		this.backing = null;
	}
	
	public boolean hasLeftOver()
	{
		return this.buffer.getBuffer().isReadable();
	}

	protected abstract IMinecraftPacket read(IPacketBuffer buffer);
}
