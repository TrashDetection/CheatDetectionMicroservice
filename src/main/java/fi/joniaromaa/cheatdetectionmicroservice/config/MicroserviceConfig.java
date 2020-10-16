package fi.joniaromaa.cheatdetectionmicroservice.config;

import java.util.List;

import lombok.Getter;

public class MicroserviceConfig implements DatabaseConfig
{
	@Getter private String databaseHost;
	@Getter private int databasePort;
	@Getter private String databaseUser;
	@Getter private String databasePass;
	@Getter private String databaseName;
	
	@Getter private boolean autoBan;
	
	@Getter private List<MinecraftVersion> minecraftVersions;
	
	public static class MinecraftVersion
	{
		@Getter private boolean enabled;
		
		@Getter private String name;
		@Getter private String gameDir;
	}
}
