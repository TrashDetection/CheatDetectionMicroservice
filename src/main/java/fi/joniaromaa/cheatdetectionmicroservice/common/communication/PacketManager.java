package fi.joniaromaa.cheatdetectionmicroservice.common.communication;

import java.util.HashMap;
import java.util.Map;

import fi.joniaromaa.cheatdetectionmicroservice.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class PacketManager
{
	private Int2ObjectMap<Class<? extends IncomingPacket>> incomingPacketsById;
	
	private Int2ObjectMap<Class<? extends OutgoingPacket>> outgoingPacketsById;
	private Map<Class<? extends OutgoingPacket>, Integer> outgoingPacketsByClass;
	
	public PacketManager()
	{
		this.incomingPacketsById = new Int2ObjectOpenHashMap<>();
		
		this.outgoingPacketsById = new Int2ObjectOpenHashMap<>();
		this.outgoingPacketsByClass = new HashMap<>();
		
		this.addIncomingPackets();
		this.addOutgoingPackets();
	}
	
	protected abstract void addIncomingPackets();
	protected abstract void addOutgoingPackets();
	
	protected void addIncomingPacket(int id, Class<? extends IncomingPacket> clazz)
	{
		this.incomingPacketsById.put(id, clazz);
	}
	
	protected void addOutgoingPacket(int id, Class<? extends OutgoingPacket> clazz)
	{
		this.outgoingPacketsById.put(id, clazz);
		this.outgoingPacketsByClass.put(clazz, id);
	}
	
	public IncomingPacket readIncomingPacket(ByteBuf in)
	{
		int packetId = ByteBufUtils.readVarInt(in);
		
		Class<? extends IncomingPacket> clazz = this.incomingPacketsById.get(packetId);
		if (clazz != null)
		{
			try
			{
				IncomingPacket packet = clazz.newInstance();
				packet.read(in);
				
				//if (in.isReadable())
				//{
					//throw new RuntimeException("Incoming packet " + clazz + " had still " + in.readableBytes() + " readable bytes left");
				//}

				return packet;
			} 
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			return null;
		}
	}
	
	public ByteBuf writeOutgoingPacket(OutgoingPacket packet)
	{
		ByteBuf out = Unpooled.buffer();
		
		this.writeOutgoingPacket(packet, out);
		
		return out;
	}
	
	public void writeOutgoingPacket(OutgoingPacket packet, ByteBuf out)
	{
		Integer packetId = this.outgoingPacketsByClass.get(packet.getClass());
		if (packetId == null)
		{
			throw new RuntimeException("Packet id not found");
		}
		
		ByteBufUtils.writeVarInt(out, packetId);
		
		packet.write(out);
	}

	public OutgoingPacket readOutgoingPacket(ByteBuf out)
	{
		int packetId = ByteBufUtils.readVarInt(out);
		
		Class<? extends OutgoingPacket> clazz = this.outgoingPacketsById.get(packetId);
		if (clazz != null)
		{
			try
			{
				OutgoingPacket packet = clazz.newInstance();
				packet.read(out);
				
				//if (out.isReadable())
				//{
					//throw new RuntimeException("Outgoing packet " + clazz + " had still " + out.readableBytes() + " readable bytes left");
				//}

				return packet;
			} 
			catch (InstantiationException | IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			return null;
		}
	}
	
	protected Class<? extends IncomingPacket> getIncomingPacket(int id)
	{
		return this.incomingPacketsById.get(id);
	}
	
	protected Class<? extends OutgoingPacket> getOutgoingPacket(int id)
	{
		return this.outgoingPacketsById.get(id);
	}
}
