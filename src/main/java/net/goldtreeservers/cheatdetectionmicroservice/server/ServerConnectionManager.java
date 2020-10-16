package net.goldtreeservers.cheatdetectionmicroservice.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.bans.BanManager;
import net.goldtreeservers.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;
import net.goldtreeservers.cheatdetectionmicroservice.db.DatabaseManager;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;

public class ServerConnectionManager
{
	private static final Logger LOGGER = LogManager.getLogger(ServerConnectionManager.class);
	
	@Getter private final DatabaseManager databaseManager;
	@Getter private final BanManager banManager;
	
	private LoadingCache<Integer, Cache<UUID, ServerConnection>> handlers;
	
	private BlockingQueue<ServerConnection> analyzeRequired;
	
	public ServerConnectionManager(DatabaseManager databaseManager, MicroserviceConfig config)
	{
		this.databaseManager = databaseManager;
		this.banManager = new BanManager(databaseManager, this, config);
		
		this.handlers = Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofMinutes(10))
				.removalListener(new RemovalListener<Integer, Cache<UUID, ServerConnection>>()
				{
					@Override
					public void onRemoval(@Nullable Integer serverId, @Nullable Cache<UUID, ServerConnection> sessions, @NonNull RemovalCause cause)
					{
						for(ServerConnection connection : sessions.asMap().values())
						{
							try
							{
								connection.close();
							}
							catch(Throwable e)
							{
								ServerConnectionManager.LOGGER.error(e);
							}
						}
					}
				})
				.build(new CacheLoader<Integer, Cache<UUID, ServerConnection>>()
				{
					@Override
					public Cache<UUID, ServerConnection> load(Integer key) throws Exception
					{
						return Caffeine.newBuilder()
								.expireAfterAccess(Duration.ofMinutes(5))
								.build();
					}
				});
		
		this.analyzeRequired = new LinkedBlockingQueue<>();
		
		Thread thread = new Thread(() ->
		{
			long lastCleanup = System.nanoTime();
			
			while (true)
			{
				try
				{
					while (true)
					{
						//Run them one by one
						ServerConnection connection = this.analyzeRequired.poll(10, TimeUnit.SECONDS);
						if (connection == null)
						{
							break;
						}
						
						connection.analyze();
						
						//If we have been stuck here for more than 10s break and execute the clean up
						if (System.nanoTime() - lastCleanup > TimeUnit.SECONDS.toNanos(10))
						{
							break;
						}
					}
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					
					break;
				}
				catch(Throwable e)
				{
					ServerConnectionManager.LOGGER.error(e);
				}
				
				lastCleanup = System.nanoTime();
				
				try
				{
					this.banManager.cleanup();
					
					//First clean up servers, so they can close open resources
					for(Cache<UUID, ServerConnection> handler : this.handlers.asMap().values())
					{
						for(ServerConnection connection : handler.asMap().values())
						{
							connection.cleanUp();
						}
						
						//Now clean up handlers itself
						handler.cleanUp();
					}
					
					//Then clear the leftover servers
					this.handlers.cleanUp();
				}
				catch(Throwable e)
				{
					ServerConnectionManager.LOGGER.error(e);
				}
			}
		});
		
		thread.setDaemon(true);
		thread.setName("Server Connection Manager Thread");
		thread.start();
	}

	public void load(Connection connection) throws SQLException
	{
		this.banManager.load(connection);
	}
	
	public ServerConnection registerConnection(ServerConnectionHandler handler)
	{
		if (handler.getConfig() == null)
		{
			throw new RuntimeException("Configuration file is missing");
		}
		
		ServerConnection connection = new ServerConnection(this, handler, UUID.randomUUID());

		Cache<UUID, ServerConnection> handlers = this.handlers.get(handler.getConfig().getUserId());
		while (true)
		{
			if (handlers.asMap().putIfAbsent(connection.getUniqueId(), connection) == null)
			{
				return connection;
			}
			
			connection.setUniqueId(UUID.randomUUID());
		}
	}
	
	public void serverPing(ServerConnectionHandler handler)
	{
		if (handler.getConfig() == null || handler.getServerConnection() == null)
		{
			return;
		}
		
		Cache<UUID, ServerConnection> handlers = this.handlers.getIfPresent(handler.getConfig().getUserId());
		if (handlers != null)
		{
			handlers.getIfPresent(handler.getServerConnection().getUniqueId()); //Refresh access time
		}
	}
	
	public void unregisterConnection(ServerConnectionHandler handler)
	{
		if (handler.getConfig() == null ||  handler.getServerConnection() == null)
		{
			return;
		}

		Cache<UUID, ServerConnection> handlers = this.handlers.getIfPresent(handler.getConfig().getUserId());
		if (handlers != null)
		{
			handlers.invalidate(handler.getServerConnection().getUniqueId());
		}
	}
	
	public ServerConnection getConnetion(ServerConnectionHandler handler, UUID uniqueId)
	{
		Cache<UUID, ServerConnection> handlers = this.handlers.getIfPresent(handler.getConfig().getUserId());
		if (handlers != null)
		{
			return handlers.getIfPresent(uniqueId);
		}
		
		return null;
	}
	
	public void requestAnalyze(ServerConnection connection)
	{
		this.analyzeRequired.add(connection);
	}
	
	public void saveData(int serverId, int userId)
	{
		for(Cache<UUID, ServerConnection> handler : this.handlers.asMap().values())
		{
			for(ServerConnection connection : handler.asMap().values())
			{
				if (connection.getHandler().getConfig().getServerId() == serverId)
				{
					connection.saveData(userId);
				}
			}
		}
	}

	public void shutdown()
	{
		this.banManager.close();
		
		for(Cache<UUID, ServerConnection> handler : this.handlers.asMap().values())
		{
			for(ServerConnection connection : handler.asMap().values())
			{
				connection.shutdown();
			}
		}
	}
	
	public void broadcastToServers(int serverId, OutgoingPacket packet)
	{
		Cache<UUID, ServerConnection> servers = this.handlers.getIfPresent(serverId);
		if (servers != null)
		{
			for(ServerConnection connection : servers.asMap().values())
			{
				connection.getHandler().getChannel().writeAndFlush(packet);
			}
		}
	}
}
