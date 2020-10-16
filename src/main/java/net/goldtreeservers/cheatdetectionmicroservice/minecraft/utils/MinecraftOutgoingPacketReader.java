package net.goldtreeservers.cheatdetectionmicroservice.minecraft.utils;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IPacketBuffer;
import net.goldtreeservers.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import net.goldtreeservers.cheatdetectionmicroservice.utils.UserPackets.Procedure;

public class MinecraftOutgoingPacketReader extends MinecraftPacketReader
{
	public MinecraftOutgoingPacketReader(MinecraftPacketManager packetManager)
	{
		super(packetManager);
	}

	@SuppressWarnings("unchecked")
	public void read(Procedure<? super IMinecraftOutgoingPacket> procedure)
	{
		super.read0((Procedure<? super IMinecraftPacket>) procedure);
	}
	
	@Override
	public IMinecraftOutgoingPacket read(IPacketBuffer buffer)
	{
		return this.getPacketManager().readOutgoingPacket(buffer);
	}
}
