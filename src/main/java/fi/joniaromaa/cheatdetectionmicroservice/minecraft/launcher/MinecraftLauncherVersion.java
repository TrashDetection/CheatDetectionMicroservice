package fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.config.MinecraftLauncherVersionConfig;
import lombok.Getter;

public class MinecraftLauncherVersion
{
	@Getter private final String name;
	
	@Getter private final Path versionFolderPath;
	@Getter private final Path configPath;
	@Getter private final Path jarPath;
	
	@Getter private final MinecraftLauncherVersionConfig config;
	
	@Getter private final List<URL> libraries;
	
	public MinecraftLauncherVersion(String name, Path versionFolderPath, Path configPath, Path jarPath, MinecraftLauncherVersionConfig config, List<URL> libraries)
	{
		this.name = name;
		
		this.versionFolderPath = versionFolderPath;
		this.configPath = configPath;
		this.jarPath = jarPath;
		
		this.config = config;
		
		this.libraries = libraries;
	}
}
