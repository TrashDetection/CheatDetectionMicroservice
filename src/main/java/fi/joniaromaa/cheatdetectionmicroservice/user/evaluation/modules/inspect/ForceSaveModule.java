package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.inspect;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.Module;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket;

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
