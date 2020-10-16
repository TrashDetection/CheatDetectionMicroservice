package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.inspect;

import fi.joniaromaa.cheatdetectionmicroservice.services.cheatdetection.CheatDetectionService;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.Module;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IPluginMessageOutgoingPacket;

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
		return CheatDetectionService.DEBUG && false;
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
		/*Object data = packet.getDataObject();
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
		}*/
	}
}
