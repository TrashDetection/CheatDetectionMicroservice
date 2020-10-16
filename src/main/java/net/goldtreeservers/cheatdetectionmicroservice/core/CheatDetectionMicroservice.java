package net.goldtreeservers.cheatdetectionmicroservice.core;

import java.io.File;

import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;
import net.goldtreeservers.cheatdetectionmicroservice.minecraft.MinecraftLoader;

public class CheatDetectionMicroservice
{
	public static final boolean DEBUG = CheatDetectionMicroservice.getEnableDebug();
	
	@Getter private MinecraftLoader minecraftLoader;
	
	@Getter private CheatDetectionService service;
	
	public CheatDetectionMicroservice(MicroserviceConfig config)
	{
		this.minecraftLoader = new MinecraftLoader();
		
		this.service = CheatDetectionServiceBoostrap.load(config);
	}
	
	public void start()
	{
		this.minecraftLoader.load(new File("clients"));
		
		this.service.start();
	}
	
	public void shutdown()
	{
		this.service.shutdown();
	}
	
	private static boolean getEnableDebug()
	{
		return System.getProperty("td.debug", "false").equalsIgnoreCase("true");
	}
}
