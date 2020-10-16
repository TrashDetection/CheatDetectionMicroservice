package fi.joniaromaa.cheatdetectionmicroservice.net.communication.outgoing;

import java.util.HashMap;
import java.util.Map;

import fi.joniaromaa.cheatdetectionmicroservice.common.communication.OutgoingPacket;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.combat.NotFacingAttackingEntityViolation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.player.combat.ReachViolation;
import io.netty.buffer.ByteBuf;

public class UserViolationsOutgoingPacket implements OutgoingPacket
{
	private static Map<Class<? extends UserViolation>, Integer> violationIds = new HashMap<>();
	
	static
	{
		UserViolationsOutgoingPacket.violationIds.put(ReachViolation.class, 0);
		UserViolationsOutgoingPacket.violationIds.put(NotFacingAttackingEntityViolation.class, 1);
	}
	
	private int sessionId;
	
	public UserViolationsOutgoingPacket(int sessionId)
	{
		this.sessionId = sessionId;
	}
	
	@Override
	public void write(ByteBuf out)
	{
		out.writeInt(this.sessionId);
		out.writeInt(0);
		/*out.writeInt(this.violations.size());
		
		for(UserViolation violation : this.violations)
		{
			ByteBufUtils.writeVarInt(out, UserViolationsOutgoingPacket.violationIds.get(violation.getClass()));
			
			violation.deserialize(out);
		}*/
	}
}
