package net.goldtreeservers.cheatdetectionmicroservice.user;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IGamemode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;
import net.goldtreeservers.cheatdetectionmicroservice.db.DatabaseManager;
import net.goldtreeservers.cheatdetectionmicroservice.mocked.utils.MockedUserPackets;
import net.goldtreeservers.cheatdetectionmicroservice.net.NetworkManager;
import net.goldtreeservers.cheatdetectionmicroservice.net.handler.ServerConnectionHandler;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConfig;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConnection;
import net.goldtreeservers.cheatdetectionmicroservice.server.ServerConnectionManager;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Location;
import net.goldtreeservers.cheatdetectionmicroservice.user.data.Position;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.combat.NotFacingAttackingEntityViolation;

public class ServerUserTest
{
	private DummyServerConnectionManager serverManager = new DummyServerConnectionManager(null);
	private DummyServerConnectionHandler connectionHandler = new DummyServerConnectionHandler(null);
	private ServerConnection serverConnection = new DummyServerConnection(this.serverManager, this.connectionHandler, null);
	private ServerUserStripepd serverUser = new ServerUserStripepd(this.serverConnection, 47);
	
	@Before
	public void setup() throws Exception
	{
		this.serverUser.prepare();
	}
	
	@Test
	public void testBanWaveBasic() throws Exception
	{
		this.serverUser.getPackets().addPacketConfirmedWithConfirmation(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.serverUser.getPackets().addIncomingPacket(new InvalidLookPlayerLookIncomingPacket());
		
		this.serverUser.addBytesToAnalyze(1, Unpooled.EMPTY_BUFFER, Unpooled.EMPTY_BUFFER, 0);
		
		assertEquals(1, this.serverUser.getViolations().size());
		//assertEquals(0, this.serverManager.getBanManager().getBan(this.serverUser.getServer().getHandler().getConfig().getServerId(), this.serverUser.getUserId()).getScore());
	}
	
	@Test
	public void testBanWaveCrafted() throws Exception
	{
		this.serverUser.getPackets().addPacketConfirmedWithConfirmation(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.serverUser.getPackets().addIncomingPacket(new InvalidLookPlayerLookIncomingPacket()); //Just to trigger

		NotFacingAttackingEntityViolation fakeViolation = new NotFacingAttackingEntityViolation(0, new Location(1, 0, 0, 0, 0), new Position(0, 0, 0));
		
		List<UserViolation> violations = new ArrayList<UserViolation>();
		violations.add(fakeViolation);
		
		assertEquals(900, fakeViolation.getViolationPoints());
		
		this.serverUser.setCraftedViolations(violations);
		this.serverUser.addBytesToAnalyze(1, Unpooled.EMPTY_BUFFER, Unpooled.EMPTY_BUFFER, 0);
		
		assertEquals(1, this.serverUser.getViolations().size());
		//assertEquals(900, this.serverManager.getBanManager().getBan(this.serverUser.getServer().getHandler().getConfig().getServerId(), this.serverUser.getUserId()).getScore());

		violations.add(fakeViolation);

		this.serverUser.addBytesToAnalyze(1, Unpooled.EMPTY_BUFFER, Unpooled.EMPTY_BUFFER, 0);
		
		//Should be increased
		assertEquals(2, this.serverUser.getViolations().size());
		//assertEquals(900 * 1 + 900 * 2, this.serverManager.getBanManager().getBan(this.serverUser.getServer().getHandler().getConfig().getServerId(), this.serverUser.getUserId()).getScore());

		this.serverUser.addBytesToAnalyze(1, Unpooled.EMPTY_BUFFER, Unpooled.EMPTY_BUFFER, 0);
		
		//Nothing changes
		assertEquals(2, this.serverUser.getViolations().size());
		//assertEquals(900 * 3 + 900 * 2, this.serverManager.getBanManager().getBan(this.serverUser.getServer().getHandler().getConfig().getServerId(), this.serverUser.getUserId()).getScore());
	}
	
	class ServerUserStripepd extends ServerUser
	{
		@Getter @Setter private List<UserViolation> craftedViolations;
		
		public ServerUserStripepd(ServerConnection server, int protocolVersion)
		{
			super(server, -1, -1, protocolVersion, -1);
		}
		
		public void addBytesToAnalyze(int version, ByteBuf incoming, ByteBuf outgoing, long time)
		{
			this.addBytesToAnalyze(version, 0, 0, incoming, outgoing, time);
		}
		
		@Override
		protected void setupPackets()
		{
			this.packets = new MockedUserPackets();
		}

		@Override
		public void createLogFile()
		{
			this.outputFile = new ZipOutputStream(new ByteArrayOutputStream());
		}
		
		@Override
		public File getServerFolder()
		{
			return new File("TEST");
		}
		
		public MockedUserPackets getPackets()
		{
			return (MockedUserPackets)this.packets;
		}
		
		@Override
		public List<UserViolation> getViolations()
		{
			return this.craftedViolations != null ? this.craftedViolations : super.getViolations();
		}

		@Override
		public void onException(Throwable e) throws IOException
		{
			throw new RuntimeException(e);
		}
	}
	
	class InvalidLookPlayerLookIncomingPacket implements IPlayerIncomingPacket
	{
		@Override
		public float getPitch()
		{
			return 180;
		}

		@Override
		public boolean isRotating()
		{
			return true;
		}
		
		@Override
		public double getX() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getY() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getZ() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float getYaw() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isMoving() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isOnGround() {
			// TODO Auto-generated method stub
			return false;
		}
	}
	
	class DummyServerConnectionHandler extends ServerConnectionHandler
	{
		public DummyServerConnectionHandler(NetworkManager networkManager)
		{
			super(networkManager);
			
			this.setConfig(new ServerConfig(69, 69));
		}
	}
	
	class DummyServerConnectionManager extends ServerConnectionManager
	{
		public DummyServerConnectionManager(DatabaseManager databaseManager)
		{
			super(databaseManager, new MicroserviceConfig());
		}
	}
	
	class DummyServerConnection extends ServerConnection
	{
		public DummyServerConnection(ServerConnectionManager manager, ServerConnectionHandler handler, UUID uniqueId)
		{
			super(manager, handler, uniqueId);
		}

		@Override
		public void queueAnalyze(ServerUser user)
		{
			try
			{
				user.analyze();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public void scheduleBan(int userId, int sessionId, int points, String fileName)
		{
		}
	}
}
