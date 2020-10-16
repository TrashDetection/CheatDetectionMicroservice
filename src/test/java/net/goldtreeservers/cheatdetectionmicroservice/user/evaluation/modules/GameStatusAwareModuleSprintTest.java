package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IGamemode;

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
		this.readIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		
		assertTrue(this.module.isSprinting());
	}
	
	@Test
	public void sprintStatusOnStop() throws Exception
	{
		this.readIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING));
		
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
