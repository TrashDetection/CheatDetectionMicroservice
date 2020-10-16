package fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets.Procedure;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftPacket;
import fi.joniaromaa.minecrafthook.common.network.IPacketBuffer;

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
