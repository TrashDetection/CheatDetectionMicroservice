package fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.config;

import java.util.List;

import lombok.Getter;

public class MinecraftLauncherVersionConfig
{
	@Getter private String inheritsFrom;
	
	@Getter private List<MinecraftLauncherVersionLibrary> libraries;
}
