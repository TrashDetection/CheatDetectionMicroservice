package fi.joniaromaa.cheatdetectionmicroservice.minecraft;

import java.net.URL;
import java.util.function.Consumer;

import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceURLClassLoader;
import fi.joniaromaa.cheatdetectionmicroservice.services.cheatdetection.CheatDetectionService;

public final class MinecraftClassLoader extends ServiceURLClassLoader
{
    static
    {
        ClassLoader.registerAsParallelCapable();
    }
	
	public MinecraftClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}

	@Override
	public URL getResource(String name)
	{
		if (name.equals("log4j2.xml"))
		{
			return CheatDetectionService.DEBUG 
					? ClassLoader.getSystemClassLoader().getResource("log4j2-minecraft-debug.xml") 
					: ClassLoader.getSystemClassLoader().getResource("log4j2-minecraft.xml");
		}
		else if (name.equals("log4j2.properties"))
		{
			return CheatDetectionService.DEBUG 
					? ClassLoader.getSystemClassLoader().getResource("log4j2-minecraft-debug.properties") 
					: ClassLoader.getSystemClassLoader().getResource("log4j2-minecraft.properties");
		}
		
		return super.getResource(name);
	}
	
    public static MinecraftClassLoader build(URL[] urls, ClassLoader parent, Consumer<MinecraftClassLoader> consumer)
    {
    	MinecraftClassLoader loader = new MinecraftClassLoader(urls, parent);
    	
    	consumer.accept(loader);
    	
    	return loader;
    }
}
