package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.inspect;

import java.io.FileWriter;
import java.util.Map.Entry;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IPluginMessageOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.shared.IPluginMessagePacket.ForgeHandshake;
import net.goldtreeservers.cheatdetectionmicroservice.core.CheatDetectionMicroservice;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules.Module;

public class DebugModule extends Module
{
	public DebugModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.addOutgoingHandlerPost(IPluginMessageOutgoingPacket.class, this::outgoingPluginMessage);
	}
	
	@Override
	public boolean pre()
	{
		return CheatDetectionMicroservice.DEBUG && false;
	}
	
	@Override
	public boolean handlesOutgoingPackets()
	{
		return true;
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
	
	private void outgoingPluginMessage(IPluginMessageOutgoingPacket packet)
	{
		Object data = packet.getDataObject();
		if (data instanceof ForgeHandshake.RegistryData)
		{
			ForgeHandshake.RegistryData registryData = (ForgeHandshake.RegistryData)data;
			
			try (FileWriter fileStream = new FileWriter(registryData.getName().replace(":", "_") + ".forge"))
			{
				for(Entry<String, Integer> id : registryData.getIds().entrySet())
				{
					fileStream.write("this.put(\"" + id.getKey() + "\", " + id.getValue() + ");");
					fileStream.write("\n");
				}
				
				fileStream.flush();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}
