package fi.joniaromaa.cheatdetectionmicroservice.minecraft;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fi.joniaromaa.cheatdetectionmicroservice.config.MicroserviceConfig;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.MinecraftLauncher;
import fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.MinecraftLauncherVersion;
import fi.joniaromaa.cheatdetectionmicroservice.service.ServiceLibraryLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceMultiClassLoader;
import fi.joniaromaa.minecrafthook.common.IMinecraftHook;
import fi.joniaromaa.minecrafthook.common.IMinecraftVersion;
import fi.joniaromaa.minecrafthook.common.MinecraftHooks;
import lombok.Data;

public class MinecraftLoader
{
	private static final Logger LOGGER = LogManager.getLogger(MinecraftLoader.class);
	
	private final MinecraftLauncher minecraftLauncher;
	
	private final Path runPath;
	
	private ClassLoader loader;
	
	static
	{
		System.setProperty("io.netty.noPreferDirect", "true"); //Minecraft classes, ugh

		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		System.setProperty("org.lwjgl.opengl.Display.noinput", "true");
		System.setProperty("org.lwjgl.opengl.Display.nomouse", "true");
		System.setProperty("org.lwjgl.opengl.Display.nokeyboard", "true");
		
		System.setProperty("org.lwjgl.util.NoChecks", "true");
		System.setProperty("org.lwjgl.util.NoFunctionChecks", "true");
	}
	
	public MinecraftLoader(MinecraftLauncher minecraftLauncher, Path runPath)
	{
		this.minecraftLauncher = minecraftLauncher;
		
		this.runPath = runPath;
	}
	
	public void buildClassLoader() throws IOException
	{
		ServiceMultiClassLoader parent = new ServiceMultiClassLoader(ServiceLibraryLoader.getExtensionClassLoader(), new ClassLoader[]
		{
			IMinecraftHook.class.getClassLoader()
		});

		this.loader = parent;
	}
	
	public void launchVersions(List<MicroserviceConfig.MinecraftVersion> versions)
	{
		versions.parallelStream().forEach(v ->
		{
			if (!v.isEnabled())
			{
				return;
			}
			
			try
			{
				this.launchVersion(v);
			}
			catch (Throwable e)
			{
				MinecraftLoader.LOGGER.warn("Failed to launch version: " + v, e);
			}
		});

		for (IMinecraftHook hook : MinecraftHooks.getHooks())
		{
			IMinecraftVersion version = hook.getVersion();
			
			MinecraftLoader.LOGGER.info("Loaded up MC version: " + version.getName());
		}
	}
	
	public void launchVersion(MicroserviceConfig.MinecraftVersion version)
	{
		MinecraftLoader.LOGGER.info("Launching version: " + version.getName());
		
		MinecraftLaunchData data = this.getMinecraftLaunchData(version);
		
		try
		{
			Thread thread = this.launchMinecraftThread(data);
			thread.join();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	private MinecraftLaunchData getMinecraftLaunchData(MicroserviceConfig.MinecraftVersion config)
	{
		MinecraftLauncherVersion mcVersion = this.minecraftLauncher.getVersion(config.getName());
		MinecraftClassLoader loader = this.getMinecraftClassLoader(config);
		
		return new MinecraftLaunchData(config, mcVersion, Launcher.getLauncher(loader));
	}

	private MinecraftClassLoader getMinecraftClassLoader(MicroserviceConfig.MinecraftVersion config)
	{
		List<URL> urls = this.minecraftLauncher.getVersionLibraries(config.getName());

		return MinecraftClassLoader.build(urls.toArray(new URL[0]), this.loader, (l) ->
		{
			l.addExludedPackage("fi.joniaromaa.minecrafthook.common.");
		});
	}
	
	private Thread launchMinecraftThread(MinecraftLaunchData data)
	{
		Thread loaderThread = new Thread(() -> this.launchMinecraft(data));
		loaderThread.setContextClassLoader(data.getLauncher().getLaunchClass().getClassLoader()); //Change the class loader for this thread so right classes are loaded from correct place
		loaderThread.setName("MinecraftLoader: " + data.getVersion().getName());
		loaderThread.setDaemon(true);
		loaderThread.start();
		
		return loaderThread;
	}
	
	private void launchMinecraft(MinecraftLaunchData data)
	{
		try
		{
			Launcher launcher = data.getLauncher();
			
			switch(launcher.getType())
			{
				case MOD_LAUNCHER:
					this.doModLaunch(data);
					break;
				case LEGACY_LAUNCHER:
					this.doLegacyLaunch(data);
					break;
				case FABRIC:
					this.doFabricLaunch(data);
					break;
				case VANILLA:
					this.doVanillaLaunch(data);
					break;
			}
		}
		catch (Throwable e)
		{
			MinecraftLoader.LOGGER.error("Failed to launch minecraft", e);
		}
	}
	
	private void doModLaunch(MinecraftLaunchData data) throws Throwable
	{
		Launcher launcher = data.getLauncher();
		
		Method mainMethod = launcher.getLaunchClass().getDeclaredMethod("main", String[].class);
		mainMethod.invoke(null, new Object[]
		{
			//Args
			new String[]
			{
	            "--launchTarget",
	            "fmlclient",
	            
	            "--fml.forgeVersion",
	            "32.0.71",
	            
	            "--fml.mcVersion",
	            "1.16.1",
	            
	            "--fml.forgeGroup",
	            "net.minecraftforge",
	            
	            "--fml.mcpVersion",
	            "20200625.160719",
	            
	            "--gameDir",
	            "test",
	            
				"--accessToken",
				"",
				
				"--version",
				"idk"
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void doLegacyLaunch(MinecraftLaunchData data) throws Throwable
	{
		Launcher launcher = data.getLauncher();
		
		//The original class loader
		ClassLoader minecraftClassLoader = Thread.currentThread().getContextClassLoader();
		
		Constructor<Void> constructor = (Constructor<Void>)launcher.getLaunchClass().getDeclaredConstructor();
		constructor.setAccessible(true);

		//After this, we have set the context class loader!
		Object instance = constructor.newInstance();

		//Mess with the class loader, otherwise it uses the wrong parent!
		ClassLoader launchClassLoader = Thread.currentThread().getContextClassLoader();
		
		//This delegated correctly between the launch class loader and the Minecraft one
		ClassLoader launchWrapperClassLoader = new MinecraftLaunchWrapperClassLoader(launchClassLoader, minecraftClassLoader);
		
		Thread.currentThread().setContextClassLoader(launchWrapperClassLoader);
		
		//We need to expose the fi.joniaromaa.minecrafthook.common package for the LaunchWrapper
		Class<?> launchClassLoaderClazz = launchClassLoader.getClass();
		
		Field classLoaderExceptionsField = launchClassLoaderClazz.getDeclaredField("classLoaderExceptions");
		classLoaderExceptionsField.setAccessible(true);
		
		Set<String> classLoaderExceptions = (Set<String>)classLoaderExceptionsField.get(launchClassLoader);
		classLoaderExceptions.add("fi.joniaromaa.minecrafthook.common.");
		
		Method mainMethod = launcher.getLaunchClass().getDeclaredMethod("launch", String[].class);
		mainMethod.setAccessible(true);

		mainMethod.invoke(instance, new Object[]
		{
			//Args
			new String[]
			{
				"--tweakClass",
				"net.minecraftforge.fml.common.launcher.FMLTweaker",
				
				"--versionType",
				"Forge",
				
				"--accessToken",
				"",
				
				"--userType",
				"legacy",
				
				"--uuid",
				"8c0d9cc3-c4fc-4b2b-b945-3726f6a6b3fb",
				
				"--username",
				"isokissa3",
				
				"--gameDir",
				this.runPath.resolve(data.getGameDir()).toString(),
			}
		});
	}
	
	private void doFabricLaunch(MinecraftLaunchData data) throws Throwable
	{
		Launcher launcher = data.getLauncher();
		
		Method mainMethod = launcher.getLaunchClass().getDeclaredMethod("main", String[].class);
		mainMethod.invoke(null, new Object[]
		{
			//Args
			new String[]
			{
				"--gameDir",
				this.runPath.resolve(data.getGameDir()).toString(),
			}
		});
	}
	
	private void doVanillaLaunch(MinecraftLaunchData data) throws Throwable
	{
		//Method mainMethod = launcher.getLaunchClass().getDeclaredMethod("main", String[].class);

		throw new RuntimeException("Not supporting basic vanilla!");
	}
	
	@Data
	private static class MinecraftLaunchData
	{
		private final MicroserviceConfig.MinecraftVersion config;
		private final MinecraftLauncherVersion version;
		
		private final Launcher launcher;
		
		Path getGameDir()
		{
			if (this.config.getGameDir() != null)
			{
				return Paths.get(this.config.getGameDir());
			}
			
			return Paths.get(this.version.getName());
		}
	}
	
	@Data
	private static class Launcher
	{
		private final Class<?> launchClass;
		private final LauncherType type;
		
		enum LauncherType
		{
			LEGACY_LAUNCHER,
			MOD_LAUNCHER,
			FABRIC,
			VANILLA
		}

		static Launcher getLauncher(MinecraftClassLoader loader)
		{
			try
			{
				Class<?> launchClass = loader.loadClass("cpw.mods.modlauncher.Launcher");
				
				return new Launcher( launchClass, Launcher.LauncherType.MOD_LAUNCHER);
			}
			catch (ClassNotFoundException ignored)
			{
			}
			
			try
			{
				Class<?> launchClass = loader.loadClass("net.minecraft.launchwrapper.Launch");

				return new Launcher(launchClass, Launcher.LauncherType.LEGACY_LAUNCHER);
			}
			catch (ClassNotFoundException ignored)
			{
			}
			
			try
			{
				Class<?> launchClass = loader.loadClass("net.fabricmc.loader.launch.knot.KnotClient");

				return new Launcher( launchClass, Launcher.LauncherType.FABRIC);
			}
			catch (ClassNotFoundException ignored)
			{
			}
			
			try
			{
				Class<?> launchClass = loader.loadClass("net.minecraft.client.main.Main");

				return new Launcher(launchClass, Launcher.LauncherType.VANILLA);
			}
			catch (ClassNotFoundException ignored)
			{
			}
			
			throw new RuntimeException("No supported launcher!");
		}
	}
}
