package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.movement;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IIncomingEntityActionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.utils.IGamemode;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.ModuleTest;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoSneakToggleViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SneakStatusUpdateViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintSneakingViolation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintStatusUpdateViolation;

public class SprintSneakingModuleTest extends ModuleTest
{	
	@Test
	public void testSneakDoubleEnable() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));

		this.readPackets(Collections.emptyList());

		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Arrays.asList(new SneakStatusUpdateViolation(true)));
	}
	
	@Test
	public void testSneakDoubleDisable() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING));

		this.readPackets(Collections.emptyList());
		
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING));
		
		this.readPackets(Arrays.asList(new SneakStatusUpdateViolation(false)));
	}
	
	@Test
	public void testSneakSprintEnableToggle() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());

		this.readPackets(Arrays.asList(new SprintSneakingViolation(), new NoSneakToggleViolation()));
	}
	
	@Test
	public void testSprintSneakEnableToggle() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Arrays.asList(new SprintSneakingViolation()));
	}
	
	@Test
	public void testSprintDoubleDisable() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SPRINTING));

		this.readPackets(Arrays.asList(new SprintStatusUpdateViolation(false)));
	}
	
	@Test
	public void testSprintToggleWhenSneaking() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());

		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING));
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void testSprintToggleWhenSneakingWrongOrder() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());

		this.readPackets(Arrays.asList(new SprintSneakingViolation(), new NoSneakToggleViolation()));
	}
	
	@Test
	public void noData() throws Exception
	{
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void testSprintToggleJoin() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addConfirmedPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));

		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void testSprintToggleRespawn() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addConfirmedPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void sneakLateToggleOff() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void noSneakLateToggleOff() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.STOP_SLEEPING));
		
		this.readPackets(Arrays.asList(new NoSneakToggleViolation()));
	}
	
	@Test
	public void sprintStatusInMiddleOfResetJoin() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.addConfirmPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addPrePacketConfirmed();
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addPostPacketConfirmed();
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void sprintStatusInMiddleOfResetRespawn() throws Exception
	{
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.addConfirmPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addPrePacketConfirmed();
		this.addIncomingPacket(IIncomingEntityActionIncomingPacket.newInstance(IIncomingEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addPostPacketConfirmed();
		
		this.readPackets(Collections.emptyList());
	}
}
