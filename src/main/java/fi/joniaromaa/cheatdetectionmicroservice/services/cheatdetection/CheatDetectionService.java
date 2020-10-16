package fi.joniaromaa.cheatdetectionmicroservice.services.cheatdetection;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;

import fi.joniaromaa.cheatdetectionmicroservice.config.MicroserviceConfig;
import fi.joniaromaa.cheatdetectionmicroservice.db.DatabaseManager;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftLoader;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.MinecraftLauncher;
import fi.joniaromaa.cheatdetectionmicroservice.net.NetworkManager;
import fi.joniaromaa.cheatdetectionmicroservice.server.ServerConnectionManager;
import fi.joniaromaa.cheatdetectionmicroservice.service.cheatdetection.ICheatDetectionService;
import io.netty.util.ResourceLeakDetector;
import lombok.Getter;

public class CheatDetectionService implements ICheatDetectionService
{
	public static final boolean DEBUG = CheatDetectionService.getEnableDebug();
	
	@Getter private Gson gson;
	
	@Getter private MicroserviceConfig config;
	
	@Getter private MinecraftLauncher minecraftLauncher;
	@Getter private MinecraftLoader minecraftLoader;
	
	@Getter private DatabaseManager databaseManager;
	
	@Getter private ServerConnectionManager serverConnection;
	
	@Getter private NetworkManager networkManager;
	
	static
	{
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
	}
	
	public CheatDetectionService()
	{
		this.gson = new Gson();
	}
	
	@Override
	public void load()
	{
		try(Reader reader = Files.newBufferedReader(Paths.get("config.json")))
		{
			this.config = this.gson.fromJson(reader, MicroserviceConfig.class);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load the config!", e);
		}

		Path clientsRootPath = Paths.get("clients");
		
		try
		{
			this.minecraftLauncher = new MinecraftLauncher(clientsRootPath.resolve("versions"), clientsRootPath.resolve("libraries"));
			this.minecraftLauncher.loadVersions();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load minecraft launcher data!", e);
		}

		try
		{
			this.minecraftLoader = new MinecraftLoader(this.minecraftLauncher, clientsRootPath.resolve("run"));
			this.minecraftLoader.buildClassLoader();
			this.minecraftLoader.launchVersions(this.config.getMinecraftVersions());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to launch required minecraft verions!", e);
		}
		
		this.databaseManager = new DatabaseManager(this.config);
		try(Connection connection = this.databaseManager.getConnection())
		{
			this.serverConnection = new ServerConnectionManager(this.databaseManager, this.config);
			this.serverConnection.load(connection);
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Failed to connect to the database", e);
		}
		
		this.networkManager = new NetworkManager(this.serverConnection);
		this.networkManager.start();
	}
	
	@Override
	public void shutdown()
	{
		this.networkManager.stop();
		
		this.serverConnection.shutdown();
	}
	
	private static boolean getEnableDebug()
	{
		return System.getProperty("td.debug", "false").equalsIgnoreCase("true");
	}
}
