package net.goldtreeservers.cheatdetectionmicroservice.utils;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import io.netty.buffer.ByteBuf;

public class UserPacketsNoOp extends UserPackets
{
	public UserPacketsNoOp()
	{
		super(null, null);
	}
    
	@Override
	public void readIncoming(Procedure<? super IMinecraftIncomingPacket> procedure)
	{
	}
    
	@Override
	public void readOutgoing(Procedure<? super IMinecraftOutgoingPacket> procedure)
	{
	}
	
	@Override
	public void setIncomingBacking(ByteBuf buf)
	{
	}
	
	@Override
	public void setOutgoingBacking(ByteBuf buf)
	{
	}
	
	@Override
	public boolean hasLeftOver()
	{
		return false;
	}
}
