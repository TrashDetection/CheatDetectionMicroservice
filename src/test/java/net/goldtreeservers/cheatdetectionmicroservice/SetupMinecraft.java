package net.goldtreeservers.cheatdetectionmicroservice;

import java.io.File;

import net.goldtreeservers.cheatdetectionmicroservice.minecraft.MinecraftLoader;

public class SetupMinecraft
{
	private static final MinecraftLoader loader = new MinecraftLoader();
	private static boolean loaded;
	
	public synchronized static void setup()
	{
		if (!SetupMinecraft.loaded)
		{
			SetupMinecraft.loaded = true;
			
			SetupMinecraft.loader.load(new File("clients"));
		}
	}
}
