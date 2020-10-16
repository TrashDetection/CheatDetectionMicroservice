package fi.joniaromaa.cheatdetectionmicroservice.mocked.utils;

import java.util.ArrayDeque;

import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IConfirmTransactionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IConfirmTransactionOutgoingPacket;
import io.netty.buffer.ByteBuf;

public class MockedUserPackets extends UserPackets
{
	private ArrayDeque<IMinecraftIncomingPacket> incomingPackets;
	private ArrayDeque<IMinecraftOutgoingPacket> outgoingPackets;
	
	public MockedUserPackets()
	{
		super(null, null);
		
		this.incomingPackets = new ArrayDeque<>();
		this.outgoingPackets = new ArrayDeque<>();
	}

	public void addIncomingPacket(IMinecraftIncomingPacket packet)
	{
		this.incomingPackets.add(packet);
	}

	public void addOutgoingPacket(IMinecraftOutgoingPacket packet)
	{
		this.outgoingPackets.add(packet);
	}

	public void addPacketConfirmed(IMinecraftOutgoingPacket packet)
	{
		this.addOutgoingPacket(IConfirmTransactionOutgoingPacket.newPreConfirmInstance());
		this.addOutgoingPacket(packet);
		this.addOutgoingPacket(IConfirmTransactionOutgoingPacket.newConfirmInstance());
	}
	
	public void addPacketConfirmedWithConfirmation(IMinecraftOutgoingPacket packet)
	{
		this.addPacketConfirmed(packet);
		this.confirmPacket();
	}
	
	public void preConfirmPacket()
	{
		this.addIncomingPacket(IConfirmTransactionIncomingPacket.newPreConfirmInstance());
	}
	
	public void postConfirmPacket()
	{
		this.addIncomingPacket(IConfirmTransactionIncomingPacket.newConfirmInstance());
	}
	
	public void confirmPacket()
	{
		this.preConfirmPacket();
		this.postConfirmPacket();
	}
	
	@Override
	public void setIncomingBacking(ByteBuf buf)
	{
		//NOP
	}

	@Override
	public void setOutgoingBacking(ByteBuf buf)
	{
		//NOP
	}
    
	@Override
	public void readIncoming(Procedure<? super IMinecraftIncomingPacket> procedure)
	{
		while (!this.incomingPackets.isEmpty())
		{
			procedure.execute(this.incomingPackets.poll());
		}
	}

	@Override
	public void readOutgoing(Procedure<? super IMinecraftOutgoingPacket> procedure)
	{
		while (!this.outgoingPackets.isEmpty())
		{
			procedure.execute(this.outgoingPackets.poll());
		}
	}
}
