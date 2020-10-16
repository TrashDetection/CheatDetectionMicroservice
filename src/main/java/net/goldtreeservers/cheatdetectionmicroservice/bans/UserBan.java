package net.goldtreeservers.cheatdetectionmicroservice.bans;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

public class UserBan
{
	private static final long BASE_BAN_TIME = TimeUnit.DAYS.toSeconds(7); //Whopping 7 days
	
	@Getter private final int id;
	@Getter private final int serverId;
	@Getter private final int userId;
	
	private Timestamp time;
	
	private AtomicInteger score;
	private HashSet<Integer> sessions;
	
	@Getter @Setter private int serverBanId;
	
	public UserBan(int id, int serverId, int userId)
	{
		this.id = id;
		this.serverId = serverId;
		this.userId = userId;
		
		this.time = Timestamp.from(Instant.now());
		
		this.score = new AtomicInteger(0);
		this.sessions = new HashSet<>();
	}
	
	public UserBan(ResultSet result) throws SQLException
	{
		this.id = result.getInt("id");
		this.serverId = result.getInt("server_id");
		this.userId = result.getInt("user_id");
		
		this.time = result.getTimestamp("time");

		int score = result.getInt("score");

		this.score = new AtomicInteger(score);
		this.sessions = new HashSet<>();
		
		Array array = result.getArray("sessions");

		try(ResultSet arrayResult = array.getResultSet())
		{
			while (arrayResult.next())
			{
				//The first is index
				int value = arrayResult.getInt(2);
				
				this.sessions.add(value);
			}
		}
		
		this.serverBanId = result.getInt("server_ban_id");
	}
	
	public void addScore(int score, int session)
	{
		this.score.addAndGet(score);
		
		synchronized(this.sessions)
		{
			this.sessions.add(session);
		}
	}
	
	public int getScore()
	{
		return this.score.get();
	}
	
	public Instant getBanTime()
	{
		Instant banTime = this.time.toInstant(); //The time, when we got marked
		banTime = banTime.plusSeconds(UserBan.BASE_BAN_TIME / Math.max(this.sessions.size(), 1)); //Add the "base" time when we get banned, reduce it by the amount of sessions
		banTime = banTime.minusSeconds(this.getScore()); //Score reduces when to ban
		
		return banTime;
	}
}
