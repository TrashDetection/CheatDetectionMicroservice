package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.inspect;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IPluginMessageIncomingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;

public class ForceSaveModule extends Module
{
	public ForceSaveModule(UserEvaluation evaluation)
	{
		super(evaluation);
	}

	@Override
	public boolean handlesOutgoingPackets()
	{
		return false;
	}

	@Override
	public boolean handlesIncomingPackets()
	{
		return true;
	}

	@Override
	public boolean handlesPluginMessages()
	{
		return true;
	}

	@Override
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		if (!this.getEvaluation().isForceSave())
		{
			if (packet instanceof IPluginMessageIncomingPacket)
			{
				IPluginMessageIncomingPacket pluginPacket = (IPluginMessageIncomingPacket)packet;
				if (pluginPacket.getChannel().equals("td:debug"))
				{
					this.getEvaluation().setForceSave(true);
				}
			}
		}
	}
}
