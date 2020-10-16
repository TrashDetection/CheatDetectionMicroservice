package fi.joniaromaa.cheatdetectionmicroservice.config;

import javax.annotation.Nonnull;

public interface DatabaseConfig
{
	public @Nonnull String getDatabaseHost();
	public int getDatabasePort();
	
	public @Nonnull String getDatabaseUser();
	public @Nonnull String getDatabasePass();
	public @Nonnull String getDatabaseName();
}
