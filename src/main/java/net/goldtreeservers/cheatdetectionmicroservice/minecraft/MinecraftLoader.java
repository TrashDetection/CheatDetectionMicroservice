package net.goldtreeservers.cheatdetectionmicroservice.minecraft;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.IMinecraftHook;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.shared.SharedMinecraftHooks;
import net.goldtreeservers.cheatdetectionmicroservice.Program;

public class MinecraftLoader
{
	private static final Logger LOGGER = LogManager.getLogger(MinecraftLoader.class);
	
	private URLClassLoader parent;
	
	static
	{
		System.setProperty("io.netty.noPreferDirect", "true"); //Minecraft classes, ugh

		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		System.setProperty("org.lwjgl.opengl.Display.noinput", "true");
		System.setProperty("org.lwjgl.opengl.Display.nomouse", "true");
		System.setProperty("org.lwjgl.opengl.Display.nokeyboard", "true");
		System.setProperty("org.lwjgl.util.NoChecks", "true");

		if (System.getProperty("org.lwjgl.librarypath") == null)
		{
			System.setProperty("org.lwjgl.librarypath", new File("native").getAbsolutePath());
		}
		
		if (System.getProperty("log4j.configuration") == null)
		{
			URL log4jConfigUrl = Program.class.getClassLoader().getResource("log4j2.xml");
			
			//Because Minecraft messes with our config
			System.setProperty("log4j.configuration", log4jConfigUrl.toString());
		}
	}

	public MinecraftLoader()
	{
		this(MinecraftLoader.getClassLoader());
	}
	
	private static URLClassLoader getClassLoader()
	{
		try
		{			
			URLClassLoader loader = new URLClassLoader(new URL[]
			{
			}, Thread.currentThread().getContextClassLoader());

			loader.loadClass("net.minecraftforge.fml.relauncher.FMLSecurityManager");
			loader.loadClass("org.lwjgl.Sys");
			
			return loader;
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public MinecraftLoader(URLClassLoader parent)
	{
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	public void load(File folder)
	{
		List<Class<?>> classes = IMinecraftHook.getClasses();

		for(File file : folder.listFiles())
		{
			if (!file.isFile())
			{
				continue;
			}
			
			try
			{
				List<URL> urls = new ArrayList<>();
				urls.add(file.toURI().toURL());

				File modulesFolder = new File(folder, file.getName().substring(0, file.getName().length() - 4));
				if (modulesFolder.exists())
				{
					try(Stream<Path> walk = Files.walk(modulesFolder.toPath()))
					{
						walk.filter(Files::isRegularFile).forEach((f) ->
						{
							File moduleFile = f.toFile();
							
							try
							{
								urls.add(moduleFile.toURI().toURL());
							}
							catch (MalformedURLException e)
							{
								throw new RuntimeException(e);
							}
						});
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
				
				for(URL url : this.parent.getURLs())
				{
					urls.add(url);
				}

				URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), this.parent);
				
				try
				{
					Thread loaderThread = new Thread(() ->
					{
						try
						{
							Class<?> launchClass = loader.loadClass("net.minecraft.launchwrapper.Launch");
							
							Constructor<Void> constructor = (Constructor<Void>) launchClass.getDeclaredConstructor();
							constructor.setAccessible(true);
							
							Object instance = constructor.newInstance();
							
							//After this, we have set the context class loader!

							//Mess with the class loader
							ClassLoader launchClassLoader = Thread.currentThread().getContextClassLoader();
							
							Class<?> launchClassLoaderClass = launchClassLoader.getClass();
							
							Field field = launchClassLoaderClass.getDeclaredField("cachedClasses");
							field.setAccessible(true);
							
							Map<String, Class<?>> cachedClasses = (Map<String, Class<?>>)field.get(launchClassLoader);
							classes.forEach((c) -> cachedClasses.put(c.getName(), c));
							
							Method mainMethod = launchClass.getDeclaredMethod("launch", String[].class);
							mainMethod.setAccessible(true);
							
							mainMethod.invoke(instance, new Object[]
							{
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
									"isokissa3"
								}
							});
							
							Program.setSeucityManagerToNull();
							
						}
						catch(Throwable e)
						{
							throw new RuntimeException(e);
						}
					});
					
					loaderThread.setName("MinecraftLoader");
					loaderThread.setDaemon(true);
					loaderThread.start();
					loaderThread.join();
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				catch (Throwable e)
				{
					try
					{
						loader.close();
					}
					catch (IOException e1)
					{
						throw new RuntimeException(e);
					}

					throw new RuntimeException(e);
				}
			}
			catch(MalformedURLException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		for (IMinecraftHook hook : SharedMinecraftHooks.getHooks())
		{
			MinecraftLoader.LOGGER.info("Loaded up MC version: " + hook.getVersion());
		}
	}
}
