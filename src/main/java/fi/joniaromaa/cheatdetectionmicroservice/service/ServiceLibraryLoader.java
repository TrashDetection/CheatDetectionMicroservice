package fi.joniaromaa.cheatdetectionmicroservice.service;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceURLClassLoader;

public final class ServiceLibraryLoader
{
	public static <S> void load(Class<S> service, ServiceURLClassLoader classLoader)
	{
		ServiceLibraryLoader.load(service, classLoader, null);
	}
	
	public static <S> void load(Class<S> service, ServiceURLClassLoader classLoader, Consumer<S> serviceConsumer)
	{
		classLoader.addExludedPackage("fi.joniaromaa.cheatdetectionmicroservice.service.");
		
		ServiceLoader<S> loader = ServiceLoader.load(service, classLoader);
		for(Iterator<S> iterator = loader.iterator(); iterator.hasNext(); )
		{
			S serviceInstance = iterator.next();
			
			if (serviceConsumer != null)
			{
				serviceConsumer.accept(serviceInstance);
			}
		}
	}
	
	public static ClassLoader getExtensionClassLoader()
	{
		return ClassLoader.getSystemClassLoader().getParent();
	}
}
