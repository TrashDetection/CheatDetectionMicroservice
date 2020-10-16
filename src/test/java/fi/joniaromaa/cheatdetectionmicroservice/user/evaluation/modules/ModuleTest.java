package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules;

import static fi.joniaromaa.cheatdetectionmicroservice.utils.AssertUtils.assetEquals;

import java.util.List;

import org.junit.Before;

import fi.joniaromaa.cheatdetectionmicroservice.mocked.user.MockedUser;
import fi.joniaromaa.cheatdetectionmicroservice.mocked.user.evaluation.MockedUserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;

public abstract class ModuleTest
{
	protected MockedUser user;
	protected MockedUserEvaluation evaluation;
	
	@Before
	public void setup() throws Exception
	{
		this.user = new MockedUser(47);
		
		this.evaluation = new MockedUserEvaluation(this.user);
		
		this.injectModules();
		
		this.evaluation.setup();
	}
	
	protected void injectModules()
	{
	}
	
	protected final void addModule(Class<? extends Module> module)
	{
		this.evaluation.addModule(module);
	}
	
	protected final void addIncomingPacket(IMinecraftIncomingPacket packet)
	{
		this.user.getPackets().addIncomingPacket(packet);
	}
	
	protected final void readIncomingPacket(IMinecraftIncomingPacket packet) throws Exception
	{
		this.addIncomingPacket(packet);
		
		this.evaluation.read(this.user.getPackets());
	}
	
	protected final void addConfirmPacket(IMinecraftOutgoingPacket packet)
	{
		this.user.getPackets().addPacketConfirmed(packet);
	}
	
	protected final void readConfirmPacket(IMinecraftOutgoingPacket packet) throws Exception
	{
		this.addConfirmPacket(packet);
		
		this.evaluation.read(this.user.getPackets());
	}
	
	protected final void addPacketConfirmed()
	{
		this.user.getPackets().confirmPacket();
	}
	
	protected final void readPacketConfirmed() throws Exception
	{
		this.addPacketConfirmed();

		this.evaluation.read(this.user.getPackets());
	}
	
	protected final void addPrePacketConfirmed()
	{
		this.user.getPackets().preConfirmPacket();
	}
	
	protected final void readPrePacketConfirmed() throws Exception
	{
		this.addPrePacketConfirmed();

		this.evaluation.read(this.user.getPackets());
	}
	
	protected final void addPostPacketConfirmed()
	{
		this.user.getPackets().postConfirmPacket();
	}
	
	protected final void readPostPacketConfirmed() throws Exception
	{
		this.addPostPacketConfirmed();

		this.evaluation.read(this.user.getPackets());
	}
	
	protected final void addConfirmedPacket(IMinecraftOutgoingPacket packet)
	{
		this.user.getPackets().addPacketConfirmedWithConfirmation(packet);
	}
	
	protected final void readConfirmedPacket(IMinecraftOutgoingPacket packet) throws Exception
	{
		this.addConfirmedPacket(packet);

		this.evaluation.read(this.user.getPackets());
	}

	protected final void readPackets(List<UserViolation> violations) throws Exception
	{
		this.evaluation.read(this.user.getPackets());
		
		assetEquals(violations, this.evaluation.getViolations());
	}
}
