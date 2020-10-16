package fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.config.MinecraftLauncherVersionConfig;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.config.MinecraftLauncherVersionLibrary;
import fi.joniaromaa.cheatdetectionmicroservice.utils.OsUtils;

public class MinecraftLauncher
{
	private static final Logger LOGGER = LogManager.getLogger(MinecraftLauncher.class);
	
	private final Path versionsPath;
	private final Path librariesPath;
	
	private Map<String, MinecraftLauncherVersion> versions;
	
	public MinecraftLauncher(Path versionsPath, Path librariesPath)
	{
		this.versionsPath = versionsPath;
		this.librariesPath = librariesPath;
		
		this.versions = new HashMap<>();
	}
	
	public void loadVersions() throws IOException
	{
		try (Stream<Path> stream = Files.walk(this.versionsPath, 1))
		{
			stream.filter(Files::isDirectory).forEach(p ->
			{
				//Exclude the root
				if (this.versionsPath.equals(p))
				{
					return;
				}
				
				try
				{
					this.loadVersion(p);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			});
		}
	}
	
	private void loadVersion(Path versionFolderPath) throws IOException
	{
		String version = versionFolderPath.getFileName().toString();
		
		MinecraftLauncher.LOGGER.info("Loading version: " + version);
		
		ArrayList<URL> jars = new ArrayList<>();
		
		Path jarPath = versionFolderPath.resolve(version + ".jar");
		if (Files.isRegularFile(jarPath))
		{
			jars.add(jarPath.toUri().toURL());
		}
		
		Path configPath = versionFolderPath.resolve(version + ".json");
		if (!Files.isRegularFile(configPath))
		{
			return;
		}

		String currentOs = OsUtils.getOs();
		
		MinecraftLauncherVersionConfig config = this.readVersionConfig(configPath);
		main: for(MinecraftLauncherVersionLibrary library : config.getLibraries())
		{
			List<MinecraftLauncherVersionLibrary.Rule> rules = library.getRules();
			if (rules != null)
			{
				for(MinecraftLauncherVersionLibrary.Rule rule : rules)
				{
					Map<String, String> os = rule.getOs();
					
					String action = rule.getAction();
					if ("allow".equals(action))
					{
						if (os != null)
						{
							continue main;
						}
					}
					else if ("disallow".equals(action))
					{
						if (os == null)
						{
							continue main;
						}
					}
				}
			}
			
			Map<String, String> natives = library.getNatives();
			if (natives != null)
			{
				String native_ = natives.get(currentOs);
				if (native_ == null)
				{
					continue;
				}
				
				MinecraftLauncherVersionLibrary.Download download = library.getDownloads();
				if (download == null)
				{
					continue;
				}
				
				Map<String, MinecraftLauncherVersionLibrary.Download.File> classifiers = download.getClassifiers();
				if (classifiers == null)
				{
					continue;
				}
				
				String arch;
				if (System.getProperty("os.arch").contains("64"))
				{
					arch = "64";
				}
				else
				{
					arch = "32";
				}

				MinecraftLauncherVersionLibrary.Download.File file = classifiers.get(native_.replace("${arch}", arch));
				
				Path filePath = this.librariesPath.resolve(Paths.get(file.getPath()));
				
				jars.add(filePath.toUri().toURL());
				
				continue;
			}

			Path libraryPath = library.getFile(this.librariesPath);

			if (!Files.isRegularFile(libraryPath))
			{
				URL downloadUrl = library.getDownloadUrl();
				if (downloadUrl != null)
				{
					MinecraftLauncher.LOGGER.info("Downloading missing library: " + libraryPath);
					
					FileUtils.copyURLToFile(downloadUrl, libraryPath.toFile());
				}
				else if (library.getDownloads() != null && library.getDownloads().getArtifact() != null)
				{
					MinecraftLauncher.LOGGER.info("Downloading missing library: " + libraryPath);
					
					MinecraftLauncherVersionLibrary.Download.File file = library.getDownloads().getArtifact();
					
					FileUtils.copyURLToFile(file.getUrl(), libraryPath.toFile());
				}
				else
				{
					MinecraftLauncher.LOGGER.warn("Missing library: " + libraryPath);
					
					continue;
				}
			}
			
			jars.add(libraryPath.toUri().toURL());
		}
		
		this.versions.put(version, new MinecraftLauncherVersion(version, versionFolderPath, configPath, jarPath, config, jars));
	}
	
	public MinecraftLauncherVersion getVersion(String version)
	{
		return this.versions.get(version);
	}
	
	public List<URL> getVersionLibraries(String version)
	{
		List<URL> jars = new ArrayList<>();
		
		MinecraftLauncherVersion mcVersion = this.versions.get(version);
		MinecraftLauncherVersionConfig config = mcVersion.getConfig();
		
		String inheritsFrom = config.getInheritsFrom();
		if (inheritsFrom != null)
		{
			jars.addAll(this.getVersionLibraries(inheritsFrom));
		}
		
		jars.addAll(mcVersion.getLibraries());
		
		return jars;
	}
	
	private MinecraftLauncherVersionConfig readVersionConfig(Path path) throws IOException
	{
		try(Reader reader = Files.newBufferedReader(path))
		{
			Gson gson = new Gson();
			
			return gson.fromJson(reader, MinecraftLauncherVersionConfig.class);
		}
	}
}
