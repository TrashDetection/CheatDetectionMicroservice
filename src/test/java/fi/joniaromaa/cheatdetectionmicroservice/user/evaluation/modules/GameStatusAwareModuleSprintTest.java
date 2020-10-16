package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.utils.IGamemode;

@Ignore //TODO: BROKEN SINCE NEW CLASSLOADING
public class GameStatusAwareModuleSprintTest extends ModuleTest
{
	private GameStatusAwareModule module;
	
	@Before
	@Override
	public void setup() throws Exception
	{
		super.setup();
		
		this.module = this.evaluation.getModule(GameStatusAwareModule.class);
	}
	
	@Test
	public void sprintStatusNoPackets() throws Exception
	{
		//No packets sent, so its "unknown"
		assertNull(this.module.isSprinting());
	}
	
	@Test
	public void sprintStatusOnStart() throws Exception
	{	
		this.readIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		
		assertTrue(this.module.isSprinting());
	}
	
	@Test
	public void sprintStatusOnStop() throws Exception
	{
		this.readIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SPRINTING));
		
		assertFalse(this.module.isSprinting());
	}
	
	@Test
	public void sprintStatusOnJoin() throws Exception
	{
		this.readConfirmedPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		
		assertFalse(this.module.isSprinting());
	}
	
	@Test
	public void sprintStatusOnRespawn() throws Exception
	{
		this.readConfirmedPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		
		assertFalse(this.module.isSprinting());
	}
}
