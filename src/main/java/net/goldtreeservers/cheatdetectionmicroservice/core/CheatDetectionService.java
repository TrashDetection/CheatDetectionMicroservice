package net.goldtreeservers.cheatdetectionmicroservice.core;

import java.sql.Connection;
import java.sql.SQLException;

import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;
import net.goldtreeservers.cheatdetectionmicroservice.db.DatabaseManager;
import net.goldtreeservers.cheatdetectionmicroservice.net.NetworkManager;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConnectionManager;

public class CheatDetectionService
{
	@Getter private DatabaseManager databaseManager;
	@Getter private ServerConnectionManager serverConnection;
	
	@Getter private NetworkManager networkManager;
	
	public CheatDetectionService(MicroserviceConfig config)
	{
		this.databaseManager = new DatabaseManager(config);
		this.serverConnection = new ServerConnectionManager(this.databaseManager, config);
		
		this.networkManager = new NetworkManager(this.serverConnection);
	}
	
	public void start()
	{
		try(Connection connection = this.databaseManager.getConnection())
		{
			this.serverConnection.load(connection);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
		
		this.networkManager.start();
	}
	
	public void shutdown()
	{
		this.networkManager.stop();
		
		this.serverConnection.shutdown();
	}
}
