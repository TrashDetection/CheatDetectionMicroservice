package net.goldtreeservers.cheatdetectionmicroservice.server;

import lombok.Getter;

public class ServerConfig
{
	@Getter private final int userId;
	@Getter private final int serverId;
	
	public ServerConfig(int userId, int serverId)
	{
		this.userId = userId;
		this.serverId = serverId;
	}
}
