package fi.joniaromaa.cheatdetectionmicroservice.minecraft.utils;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets.Procedure;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftPacket;
import fi.joniaromaa.minecrafthook.common.network.IPacketBuffer;

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
