package fi.joniaromaa.cheatdetectionmicroservice.utils;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils.MinecraftIncomingPacketReader;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils.MinecraftOutgoingPacketReader;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import io.netty.buffer.ByteBuf;

public class UserPackets
{
	private MinecraftIncomingPacketReader incomingReader;
	private MinecraftOutgoingPacketReader outgoingReader;
	
	public UserPackets(MinecraftPacketManager packetManager)
	{
		this(new MinecraftIncomingPacketReader(packetManager), new MinecraftOutgoingPacketReader(packetManager));
	}
	
	public UserPackets(MinecraftIncomingPacketReader incomingReader, MinecraftOutgoingPacketReader outgoingReader)
	{
		this.incomingReader = incomingReader;
		this.outgoingReader = outgoingReader;
	}
    
	public void readIncoming(Procedure<? super IMinecraftIncomingPacket> procedure)
	{
		this.incomingReader.read(procedure);
	}
    
	public void readOutgoing(Procedure<? super IMinecraftOutgoingPacket> procedure)
	{
		this.outgoingReader.read(procedure);
	}
	
	public void setIncomingBacking(ByteBuf buf)
	{
		this.incomingReader.setBacking(buf);
	}
	
	public void setOutgoingBacking(ByteBuf buf)
	{
		this.outgoingReader.setBacking(buf);
	}
	
	public boolean hasLeftOver()
	{
		return this.incomingReader.hasLeftOver() || this.outgoingReader.hasLeftOver();
	}
	
	public static interface Procedure<T>
	{
	    public void execute(T object);
	}
}
