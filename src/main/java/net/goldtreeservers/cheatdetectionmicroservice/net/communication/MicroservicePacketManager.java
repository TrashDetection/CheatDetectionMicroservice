package net.goldtreeservers.cheatdetectionmicroservice.net.communication;

import net.goldtreeservers.cheatdetectionmicroservice.common.communication.PacketManager;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.DisconnectIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.LoginIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.PongIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.RegisterServerIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.RequestUserViolationsIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.RestoreServerIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.UserBannedIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.incoming.UserDataIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.BanUserOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.DisconnectOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.PingOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.ServerRegisteredOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.SessionRestoreFailedOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.UserViolationsOutgoingPacket;

public class MicroservicePacketManager extends PacketManager
{
	@Override
	protected void addIncomingPackets()
	{
		this.addIncomingPacket(0, LoginIncomingPacket.class);
		this.addIncomingPacket(1, RegisterServerIncomingPacket.class);
		this.addIncomingPacket(2, RestoreServerIncomingPacket.class);
		this.addIncomingPacket(3, PongIncomingPacket.class);
		this.addIncomingPacket(4, DisconnectIncomingPacket.class);
		this.addIncomingPacket(5, UserDataIncomingPacket.class);
		this.addIncomingPacket(6, RequestUserViolationsIncomingPacket.class);
		this.addIncomingPacket(7, UserBannedIncomingPacket.class);
	}

	@Override
	protected void addOutgoingPackets()
	{
		this.addOutgoingPacket(0, ServerRegisteredOutgoingPacket.class);
		this.addOutgoingPacket(1, PingOutgoingPacket.class);
		this.addOutgoingPacket(2, DisconnectOutgoingPacket.class);
		this.addOutgoingPacket(3, SessionRestoreFailedOutgoingPacket.class);
		this.addOutgoingPacket(4, UserViolationsOutgoingPacket.class);
		this.addOutgoingPacket(5, BanUserOutgoingPacket.class);
	}
}
