package fi.joniaromaa.cheatdetectionmicroservice.user;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnection;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.ViolationType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class ServerUser extends BaseUser
{
	@Getter private final ServerConnection server;
	
	@Getter private final int sessionId;
	@Getter private final long started;
	
	private int nextServerVersion;
	
	private ConcurrentLinkedQueue<UserAnalyzationData> analyzationDataQueue;

	private AtomicBoolean requiresAnalyzation;
	private AtomicBoolean analyzing;
	
	@Getter @Setter private boolean forceSave;

	public ServerUser(ServerConnection server, int sessionId, int userId, int protocolVersion, int version)
	{
		super(userId, protocolVersion);
		
		this.server = server;
		
		this.sessionId = sessionId;
		this.started = System.currentTimeMillis();
		
		this.nextServerVersion = version;

		this.requiresAnalyzation = new AtomicBoolean(false);
		this.analyzing = new AtomicBoolean(false);
		
		this.evaluation = new UserEvaluation(this);
		
		this.analyzationDataQueue = new ConcurrentLinkedQueue<>();
	}
	
	@Override
	public void writeUserInfoExtra(PrintWriter writer)
	{
		writer.println("Session Id: " + this.sessionId);

		writer.println("Server Id: " + this.server.getHandler().getConfig().getServerId());
		writer.println("Server Unique Id: " + this.server.getUniqueId());
		writer.println("Server From: " + this.server.getHandler().getChannel().remoteAddress());
		
		writer.println("Started: " + this.started);
	}
	
	public void addBytesToAnalyze(int version, long incomingHash, long outgoingHash, ByteBuf incoming, ByteBuf outgoing, long time)
	{
		this.analyzationDataQueue.add(new UserAnalyzationData(incomingHash, outgoingHash, incoming, outgoing, time));

		if (this.requiresAnalyzation.compareAndSet(false, true))
		{
			this.server.queueAnalyze(this);
		}
	}
	
	public void analyze() throws Exception
	{
		if (this.analyzing.compareAndSet(false, true))
		{
			this.requiresAnalyzation.set(false);
			
			try
			{
				while (!this.analyzationDataQueue.isEmpty())
				{
					UserAnalyzationData data = this.analyzationDataQueue.poll();
					
					this.timeAnalyzed += data.timeAnalyzed;

					try
					{
						this.addIncomingBytes(data.incomingHash, data.incoming);
						this.addOutgoingBytes(data.outgoingHash, data.outgoing);
					}
					finally
					{
						this.version++;
					}
					
					this.evaluation.read(this.packets);
					
					int ponts = 0;

					boolean ban = false;
					boolean log = false;
					
					for(UserViolation violation : this.getViolations())
					{
						if (violation.getType() == ViolationType.CRITICAL)
						{
							ban = true;
						}
						else if (violation.getType() == ViolationType.WARN)
						{
							log = true;
						}
						
						ponts += violation.getViolationPoints();
					}
					
					if (ban)
					{
						this.server.scheduleBan(this.getUserId(), this.getSessionId(), ponts, this.getLogFile().getName());
					}
					else if (log || this.forceSave)
					{
						this.server.log(this.getUserId(), this.getSessionId(), this.getLogFile().getName());
					}
				}
			}
			finally
			{
				this.analyzing.set(false);
			}
		}
	}

	public boolean versionMatch(int version)
	{
		return ++this.nextServerVersion == version;
	}
	
	public File getServerFolder()
	{
		return Paths.get(Integer.toString(this.getServer().getHandler().getConfig().getUserId()), Integer.toString(this.getServer().getHandler().getConfig().getServerId()), this.getServer().getUniqueId().toString().replace("-", "")).toFile();
	}

	@Override
	public File getLogFileFolder()
	{
		return new File("logs", this.getServerFolder().getPath());
	}

	@Override
	public File getLogFile()
	{
		return new File(this.getUserId() + "_" + this.getSessionId() + "_" + this.getStarted() + ".zip");
	}
	
	private static class UserAnalyzationData
	{
		private final long incomingHash;
		private final long outgoingHash;
		
		private final ByteBuf incoming;
		private final ByteBuf outgoing;
		
		private final long timeAnalyzed;
		
		public UserAnalyzationData(long incomingHash, long outgoingHash, ByteBuf incoming, ByteBuf outgoing, long timeAnalyzed)
		{
			this.incoming = incoming;
			this.outgoing = outgoing;
			
			this.incomingHash = incomingHash;
			this.outgoingHash = outgoingHash;
			
			this.timeAnalyzed = timeAnalyzed;
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("Server User | Id: %d | Session Id: %d | Server Id: %d | Server Session: %s | Protocol: %d", this.getUserId(), this.getSessionId(), this.server.getHandler().getConfig().getServerId(), this.server.getUniqueId(), this.getProtocolVersion());
	}
}
