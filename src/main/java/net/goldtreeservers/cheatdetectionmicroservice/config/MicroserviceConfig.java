package net.goldtreeservers.cheatdetectionmicroservice.config;

import lombok.Getter;

public class MicroserviceConfig implements DatabaseConfig
{
	@Getter private String databaseHost;
	@Getter private int databasePort;
	@Getter private String databaseUser;
	@Getter private String databasePass;
	@Getter private String databaseName;
	
	@Getter private boolean autoBan;
}
