package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IChunkOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.utils.IGamemode;

@Ignore //TODO: BROKEN SINCE NEW CLASSLOADING
public class GameStatusAwareModuleInGameTest extends ModuleTest
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
	public void noPackets()
	{
		assertFalse(this.module.isInGame());
	}
	
	@Test
	public void onPlayerPacket() throws Exception
	{
		this.readIncomingPacket(IPlayerIncomingPacket.ground());
		
		assertTrue(this.module.isInGame()); //They sent player packet, so the player is in world
	}
	
	@Test
	public void onJoin() throws Exception
	{
		this.onPlayerPacket();
		
		this.readConfirmedPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));

		assertFalse(this.module.isInGame()); //Not yet in-game, only after the first packet
	}
	
	@Test
	public void onRespawn() throws Exception
	{
		this.onPlayerPacket();
		
		this.readConfirmedPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));

		assertFalse(this.module.isInGame()); //Not yet in-game, only after the first packet
	}
	
	@Test
	public void onRemoveChunk() throws Exception
	{
		this.onPlayerPacket();
		
		final int x = 48;
		final int z = 48;

		this.addIncomingPacket(IPlayerIncomingPacket.ground(x, 0, z));
		this.addIncomingPacket(IPlayerIncomingPacket.ground()); //Make sure non moving wont effect this
		this.readConfirmedPacket(IChunkOutgoingPacket.chunkUnload(x / 16, z / 16));
		
		assertFalse(this.module.isInGame()); //Rip, they got rekt
	}
}
