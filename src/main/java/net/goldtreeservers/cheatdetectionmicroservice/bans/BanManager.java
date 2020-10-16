package net.goldtreeservers.cheatdetectionmicroservice.bans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;
import net.goldtreeservers.cheatdetectionmicroservice.db.DatabaseManager;
import net.goldtreeservers.cheatdetectionmicroservice.net.communication.outgoing.BanUserOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConnectionManager;

public class BanManager
{
	private static final Logger LOGGER = LogManager.getLogger(BanManager.class);
	
	private final DatabaseManager databaseManager;
	private final ServerConnectionManager serverManager;
	
	private Map<Integer, UserBan> bans;
	
	private Cache<Integer, Integer> waitingBans;
	
	public BanManager(DatabaseManager databaseManager, ServerConnectionManager serverManager, MicroserviceConfig config)
	{
		this.databaseManager = databaseManager;
		this.serverManager = serverManager;
		
		this.bans = new ConcurrentHashMap<>();
		
		this.waitingBans = Caffeine.newBuilder()
				.expireAfterWrite(Duration.ofMinutes(1))
				.removalListener(new RemovalListener<Integer, Integer>()
				{
					@Override
					public void onRemoval(@Nullable Integer banId, @Nullable Integer serverId, @NonNull RemovalCause cause)
					{
						if (cause != RemovalCause.REPLACED)
						{
							BanManager.this.markBanResolvedDb(banId, serverId);
						}
					}
				})
				.build();
		
		if (config.isAutoBan())
		{
			Thread thread = new Thread(this::autoBan);
			thread.setDaemon(true);
			thread.setName("AutoBan worker");
			thread.start();
		}
	}

	public void load(Connection connection) throws SQLException
	{
		try(Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT b.id, b.server_id, b.user_id, MIN(v.time) AS time, SUM(v.score) AS score, ARRAY_AGG(v.session_id) AS sessions, b.server_ban_id"
						+ " FROM base.violations v"
						+ " LEFT JOIN base.bans b ON b.id = v.ban_id"
						+ " WHERE b.banned IS NULL"
						+ " GROUP BY b.id"))
		{
			while (result.next())
			{
				UserBan ban = new UserBan(result);
				
				this.bans.put(ban.getId(), ban);
			}
		}
	}
	
	public void addBan(int serverId, UUID serverSession, int userId, int sessionId, int points, String fileName)
	{
		ForkJoinPool.commonPool().execute(() ->
		{
			try (Connection connection = this.databaseManager.getConnection();
					PreparedStatement statement = connection.prepareStatement("WITH ban AS(INSERT INTO base.bans(server_id, user_id) VALUES(?, ?)"
							+ " ON CONFLICT(server_id, user_id) WHERE banned IS NULL"
							+ " DO UPDATE SET user_id = EXCLUDED.user_id RETURNING id, server_id, user_id)"
							
							+ " INSERT INTO base.violations(server_id, server_session, user_id, session_id, score, file_name, ban_id)"
							+ " SELECT server_id, ?::uuid, user_id, ?, ?, ?, id FROM ban"
							+ " ON CONFLICT (server_id, server_session, user_id, session_id)"
							+ " DO UPDATE SET score = violations.score + excluded.score, ban_id = (SELECT id FROM ban)"
							+ " RETURNING ban_id"))
			{
				statement.setInt(1, serverId);
				statement.setInt(2, userId);
				
				statement.setString(3, serverSession.toString());
				statement.setInt(4, sessionId);
				statement.setInt(5, points);
				statement.setString(6, fileName);
				
				try(ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						int id = result.getInt("ban_id");
						
						UserBan ban = this.bans.get(id);
						if (ban == null)
						{
							ban = new UserBan(id, serverId, userId);
							
							UserBan oldBan = this.bans.putIfAbsent(id, ban);
							if (oldBan != null)
							{
								ban = oldBan;
							}
						}
						
						ban.addScore(points, sessionId);
					}
				}
			}
			catch (SQLException e)
			{
				BanManager.LOGGER.fatal(e);
			}
		});
	}

	public void log(int serverId, UUID serverSession, int userId, int sessionId, String fileName)
	{
		ForkJoinPool.commonPool().execute(() ->
		{
			try (Connection connection = this.databaseManager.getConnection();
					PreparedStatement statement = connection.prepareStatement("INSERT INTO base.log(server_id, server_session, user_id, session_id, file_name)"
							+ " VALUES(?, ?::uuid, ?, ?, ?)"
							+ " ON CONFLICT DO NOTHING"))
			{
				statement.setInt(1, serverId);
				statement.setString(2, serverSession.toString());
				
				statement.setInt(3, userId);
				statement.setInt(4, sessionId);
				statement.setString(5, fileName);
				statement.execute();
			}
			catch (SQLException e)
			{
				BanManager.LOGGER.fatal(e);
			}
		});
	}

	public void markBanResolvedServer(int banId, int serverId)
	{
		this.waitingBans.put(banId, serverId);
	}

	public void markBanResolved(int banId)
	{
		UserBan ban = this.bans.remove(banId);
		if (ban != null)
		{
			ForkJoinPool.commonPool().execute(() ->
			{
				try (Connection connection = BanManager.this.databaseManager.getConnection();
						PreparedStatement statement = connection.prepareStatement("UPDATE base.bans SET banned = NOW() WHERE banned IS NULL AND id = ?"))
				{
					statement.setInt(1, banId);
					
					statement.execute();
				}
				catch (SQLException e)
				{
					BanManager.LOGGER.fatal(e);
				}
			});
		}
	}
	
	private void markBanResolvedDb(int banId, int serverId)
	{
		ForkJoinPool.commonPool().execute(() ->
		{
			try (Connection connection = BanManager.this.databaseManager.getConnection();
					PreparedStatement statement = connection.prepareStatement("UPDATE base.bans SET banned = NOW() WHERE banned IS NULL AND server_ban_id = ? AND server_id = ? RETURNING id"))
			{
				statement.setInt(1, banId);
				statement.setInt(2, serverId);
				
				try(ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						int id = result.getInt("id");
						
						this.bans.remove(id);
					}
				}
			}
			catch (SQLException e)
			{
				BanManager.LOGGER.fatal(e);
			}
		});
	}
	
	private void autoBan()
	{
		int ticker = 0;
		
		while (true)
		{
			try
			{
				ticker++;
				
				if (ticker % (60 * 33) == 0) //Every 33h
				{
					System.out.println("Executing special ban wave #4");
					
					this.scheduledBans(Instant.now().plus(5, ChronoUnit.DAYS)); //People that are at least 5 days from ban
				}
				else if (ticker % 60 == 0) //Every 1h
				{
					System.out.println("Executing special ban wave #3");
					
					this.scheduledBans(Instant.now().plus(1, ChronoUnit.DAYS)); //People that are 1 day from ban
				}
				else if (ticker % 15 == 0) //Every 15mins
				{
					System.out.println("Executing special ban wave #2");
					
					this.scheduledBans(Instant.now().plus(6, ChronoUnit.HOURS)); //People that are 6 hours from ban
				}
				else if (ticker % 5 == 0) //Every 5mins
				{
					System.out.println("Executing special ban wave #1");
					
					this.scheduledBans(Instant.now().plus(33, ChronoUnit.MINUTES)); //People that are 33 minutes from ban
				}
				else //Ebery minute
				{
					System.out.println("Executing ban wave");
					
					this.scheduledBans(); //People that have expired hard core
				}
				
				Thread.sleep(TimeUnit.MINUTES.toMillis(1)); //Every minute
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				
				break;
			}
			catch(Throwable e)
			{
				BanManager.LOGGER.error(e);
			}
		}
	}

	public void scheduledBans()
	{
		this.scheduledBans(false);
	}
	
	public void scheduledBans(Instant time)
	{
		this.scheduledBans(false, time);
	}
	
	public void scheduledBans(boolean forced)
	{
		Instant now = Instant.now();
		
		this.scheduledBans(forced, now);
	}
	
	public void scheduledBans(boolean forced, Instant time)
	{
		this.bans.values().forEach((b) ->
		{
			if (forced || time.isAfter(b.getBanTime()))
			{
				if (b.getServerBanId() == 0)
				{
					try (Connection connection = BanManager.this.databaseManager.getConnection();
							PreparedStatement statement = connection.prepareStatement("UPDATE base.bans SET server_ban_id = COALESCE(server_ban_id, nextval('base.bans_server_ban_id_seq'::regclass)) WHERE id = ? RETURNING server_ban_id"))
					{
						statement.setInt(1, b.getId());
						
						try(ResultSet result = statement.executeQuery())
						{
							if (result.next())
							{
								b.setServerBanId(result.getInt("server_ban_id"));
							}
							else
							{
								return;
							}
						}
					}
					catch (SQLException ex)
					{
						BanManager.LOGGER.fatal(ex);
						
						return;
					}
				}
				
				this.serverManager.broadcastToServers(b.getServerId(), new BanUserOutgoingPacket(b.getServerBanId(), b.getUserId()));
				
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
		});
	}

	public void showBans()
	{
		System.out.println("Bans waiting: " + this.bans.size());
		
		this.bans.values().forEach((b) ->
		{
			for(UserBan ban : this.bans.values())
			{
				System.out.printf("Id: %d | Server: %d | User: %d | Time: %s\n", ban.getId(), ban.getServerId(), ban.getUserId(), ban.getBanTime());
			}
		});
	}
	
	public void cleanup()
	{
		this.waitingBans.cleanUp();
	}
	
	public void close()
	{
		this.waitingBans.cleanUp();
		this.waitingBans.invalidateAll();
		this.waitingBans.cleanUp();
		
		ForkJoinPool.commonPool().awaitQuiescence(60, TimeUnit.SECONDS);
	}
}
