package net.goldtreeservers.cheatdetectionmicroservice.server;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import net.goldtreeservers.cheatdetectionmicroservice.user.ServerUser;

public class ServerConnection
{
	private static final Logger LOGGER = LogManager.getLogger(ServerConnection.class);
	
	@Getter private final ServerConnectionManager manager;
	@Getter @Setter private ServerConnectionHandler handler;
	
	@Getter @Setter(AccessLevel.PACKAGE) private UUID uniqueId;
	
	private Cache<Integer, ServerUser> users;
	
	private BlockingQueue<ServerUser> needsReevaluation;
	
	private AtomicBoolean needsAnalyzation;
	
	public ServerConnection(ServerConnectionManager manager, ServerConnectionHandler handler, UUID uniqueId)
	{
		this.manager = manager;
		this.handler = handler;
		
		this.uniqueId = uniqueId;

		this.users = Caffeine.newBuilder()
				.expireAfterAccess(Duration.ofMinutes(3))
				.removalListener(new RemovalListener<Integer, ServerUser>()
				{
					@Override
					public void onRemoval(@Nullable Integer sessionId, @Nullable ServerUser user, @NonNull RemovalCause cause)
					{
						try
						{
							user.close();
						}
						catch (Throwable e)
						{
							ServerConnection.LOGGER.error(e);
						}
					}
				}).build();
		
		this.needsReevaluation = new LinkedBlockingQueue<>();
		
		this.needsAnalyzation = new AtomicBoolean(false);
	}
	
	public ServerUser newUserData(int sessionId, int userId, int protocolVersion, int version, boolean incomplete) throws Exception
	{
		ServerUser user = this.users.getIfPresent(sessionId);
		if (user == null || !user.versionMatch(version))
		{
			if (incomplete)
			{
				return null;
			}
			
			user = new ServerUser(this, sessionId, userId, protocolVersion, version);
			user.prepare();
			
			this.users.put(sessionId, user);
		}
		
		return user;
	}
	
	public ServerUser getUser(int sessionId)
	{
		return this.users.getIfPresent(sessionId);
	}
	
	public void removeUser(int sessionId)
	{
		this.users.invalidate(sessionId);
	}

	public void analyze()
	{
		this.needsAnalyzation.set(false);

		//Make sure we get all
		while (this.needsReevaluation.size() > 0)
		{
			List<ServerUser> evaluate = new ArrayList<>(this.needsReevaluation.size());
			if (this.needsReevaluation.drainTo(evaluate) > 0)
			{
				evaluate.parallelStream().forEach((u) ->
				{
					try
					{
						u.analyze();
					}
					catch(Throwable e)
					{
						ServerConnection.LOGGER.error(e);
						
						this.removeUser(u.getSessionId());
					}
				});
			}
		}
	}
	
	public void cleanUp()
	{
		ServerConnection.LOGGER.info("Server: " + this.handler.getConfig().getServerId() + " | Users: " + this.users.estimatedSize() + " | Session: " + this.uniqueId);
		
		this.users.cleanUp();
	}
	
	public void queueAnalyze(ServerUser user)
	{
		this.needsReevaluation.add(user);
		
		if (this.needsAnalyzation.compareAndSet(false, true))
		{
			this.manager.requestAnalyze(this);
		}
	}

	public void scheduleBan(int userId, int sessionId, int points, String fileName)
	{
		this.manager.getBanManager().addBan(this.handler.getConfig().getServerId(), this.uniqueId, userId, sessionId, points, fileName);
	}

	public void log(int userId, int sessionId, String fileName)
	{
		this.manager.getBanManager().log(this.handler.getConfig().getServerId(), this.uniqueId, userId, sessionId, fileName);
	}

	public void saveData(int userId)
	{
		ServerUser user = this.users.getIfPresent(userId);
		if (user != null)
		{
			user.setForceSave(true);
		}
	}

	public void shutdown()
	{
		this.close();
	}

	public void close()
	{
		for(ServerUser user : this.users.asMap().values())
		{
			try
			{
				user.close();
			}
			catch (Throwable e)
			{
				ServerConnection.LOGGER.error(e);
			}
		}
	}
}
