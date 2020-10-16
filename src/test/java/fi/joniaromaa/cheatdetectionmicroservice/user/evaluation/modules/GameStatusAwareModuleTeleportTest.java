package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IPlayerPositionAndLookOutgoingPacket;

@Ignore //TODO: BROKEN SINCE NEW CLASSLOADING
public class GameStatusAwareModuleTeleportTest extends ModuleTest
{
	private GameStatusAwareModule module;
	private JustTeleportedModule teleportModule;
	
	@Before
	@Override
	public void setup() throws Exception
	{
		super.setup();
		
		this.module = this.evaluation.getModule(GameStatusAwareModule.class);
		this.teleportModule = this.module.getSubmodule(JustTeleportedModule.class);
	}
	
	@Override
	protected void injectModules()
	{
		this.addModule(JustTeleportedModule.class);
	}
	
	@Test
	public void justTeleportedNoLag() throws Exception
	{
		final double x = 4;
		final double y = 2;
		final double z = 0;
		
		assertFalse(this.module.isJustTeleported()); //On start its false
		
		this.readConfirmPacket(IPlayerPositionAndLookOutgoingPacket.newInstance(x, y, z));
		
		assertFalse(this.module.isJustTeleported()); //Not false after sending the teleport
		
		this.readIncomingPacket(IPlayerIncomingPacket.ground());
		
		assertFalse(this.module.isJustTeleported()); //Default move is fine
		
		this.readPacketConfirmed();
		
		assertFalse(this.module.isJustTeleported()); //Not until we are actually in the "teleport" state!

		this.teleportModule.nextIsTeleport(); //The next player packet is the teleport
		
		this.readIncomingPacket(IPlayerIncomingPacket.fly(x, y, z));
		
		assertFalse(this.module.isJustTeleported()); //Back to false
	}
	
	@Test
	public void teleportPacketNoMessInGame() throws Exception
	{
		final double x = 4;
		final double y = 2;
		final double z = 0;
		
		this.teleportModule.nextIsTeleport(); //The next player packet is the teleport
		
		this.addConfirmPacket(IPlayerPositionAndLookOutgoingPacket.newInstance(x, y, z));
		this.addPacketConfirmed();
		this.readIncomingPacket(IPlayerIncomingPacket.fly(x, y, z));
		
		assertFalse(this.module.isInGame()); //Sending teleport packet confirmation does not put them in-game
	}
	
	public static class JustTeleportedModule extends GameStatusAwareSubmodule
	{
		private boolean nextTeleported;
		
		public JustTeleportedModule(GameStatusAwareModule module, UserEvaluation evaluation)
		{
			super(module, evaluation);
			
			this.addIncomingHandlerPre(IPlayerIncomingPacket.class, this::handlePlayer);
		}
		
		private void handlePlayer(IPlayerIncomingPacket packet)
		{
			if (this.getModule().isJustTeleported())
			{
				if (!this.nextTeleported)
				{
					throw new IllegalStateException("Unexcepted teleport");
				}
				else
				{
					this.nextTeleported = true;
				}
				
				return;
			}
			
			if (this.nextTeleported)
			{
				throw new IllegalStateException("Should have been teleport");
			}
		}
		
		public void nextIsTeleport()
		{
			this.nextTeleported = true;
		}
	}
}
