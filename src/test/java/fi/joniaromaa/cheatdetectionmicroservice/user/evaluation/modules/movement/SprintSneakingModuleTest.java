package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.movement;
import java.util.Arrays;
import java.util.Collections;

import fi.joniaromaa.minecrafthook.common.network.incoming.IEntityActionIncomingPacket;
import org.junit.Ignore;
import org.junit.Test;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.ModuleTest;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.NoSneakToggleViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SneakStatusUpdateViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintSneakingViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.movement.SprintStatusUpdateViolation;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPlayerIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IJoinOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IRespawnOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.utils.IGamemode;

@Ignore //TODO: BROKEN SINCE NEW CLASSLOADING
public class SprintSneakingModuleTest extends ModuleTest
{	
	@Test
	public void testSneakDoubleEnable() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));

		this.readPackets(Collections.emptyList());

		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Arrays.asList(new SneakStatusUpdateViolation(true)));
	}
	
	@Test
	public void testSneakDoubleDisable() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SNEAKING));

		this.readPackets(Collections.emptyList());
		
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SNEAKING));
		
		this.readPackets(Arrays.asList(new SneakStatusUpdateViolation(false)));
	}
	
	@Test
	public void testSneakSprintEnableToggle() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());

		this.readPackets(Arrays.asList(new SprintSneakingViolation(), new NoSneakToggleViolation()));
	}
	
	@Test
	public void testSprintSneakEnableToggle() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Arrays.asList(new SprintSneakingViolation()));
	}
	
	@Test
	public void testSprintDoubleDisable() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SPRINTING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SPRINTING));

		this.readPackets(Arrays.asList(new SprintStatusUpdateViolation(false)));
	}
	
	@Test
	public void testSprintToggleWhenSneaking() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());

		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SNEAKING));
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void testSprintToggleWhenSneakingWrongOrder() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
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
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addConfirmedPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));

		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void testSprintToggleRespawn() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addConfirmedPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void sneakLateToggleOff() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void noSneakLateToggleOff() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addIncomingPacket(IPlayerIncomingPacket.ground());
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SPRINTING));
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.STOP_SLEEPING));
		
		this.readPackets(Arrays.asList(new NoSneakToggleViolation()));
	}
	
	@Test
	public void sprintStatusInMiddleOfResetJoin() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.addConfirmPacket(IJoinOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addPrePacketConfirmed();
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addPostPacketConfirmed();
		
		this.readPackets(Collections.emptyList());
	}
	
	@Test
	public void sprintStatusInMiddleOfResetRespawn() throws Exception
	{
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		
		this.addConfirmPacket(IRespawnOutgoingPacket.newInstance(IGamemode.Type.SURVIVAL));
		this.addPrePacketConfirmed();
		this.addIncomingPacket(IEntityActionIncomingPacket.newInstance(IEntityActionIncomingPacket.Action.START_SNEAKING));
		this.addPostPacketConfirmed();
		
		this.readPackets(Collections.emptyList());
	}
}
