package net.goldtreeservers.cheatdetectionmicroservice.minecraft.utils;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IPacketBuffer;
import net.goldtreeservers.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import net.goldtreeservers.cheatdetectionmicroservice.utils.UserPackets.Procedure;

public class MinecraftIncomingPacketReader extends MinecraftPacketReader
{
	public MinecraftIncomingPacketReader(MinecraftPacketManager packetManager)
	{
		super(packetManager);
	}
	
	@SuppressWarnings("unchecked")
	public void read(Procedure<? super IMinecraftIncomingPacket> procedure)
	{
		super.read0((Procedure<? super IMinecraftPacket>) procedure);
	}

	@Override
	public IMinecraftIncomingPacket read(IPacketBuffer buffer)
	{
		return this.getPacketManager().readIncomingPacket(buffer);
	}
}
