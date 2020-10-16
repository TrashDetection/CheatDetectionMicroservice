package fi.joniaromaa.cheatdetectionmicroservice;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftLoader;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.MinecraftLauncher;

public class SetupMinecraft
{
	private static final MinecraftLauncher launcher = new MinecraftLauncher(Paths.get("clients", "versions"), Paths.get("clients", "libraries"));
	private static final MinecraftLoader loader = new MinecraftLoader(SetupMinecraft.launcher, Paths.get("clients", "run"));
	
	private static boolean loaded;
	
	public synchronized static void setup() throws IOException
	{
		if (!SetupMinecraft.loaded)
		{
			SetupMinecraft.loaded = true;
			
			SetupMinecraft.launcher.loadVersions();
			SetupMinecraft.loader.launchVersions(Arrays.asList());
		}
	}
}
