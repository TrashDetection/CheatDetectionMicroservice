package net.goldtreeservers.cheatdetectionmicroservice.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

import net.goldtreeservers.cheatdetectionmicroservice.config.MicroserviceConfig;

public class CheatDetectionServiceBoostrap
{
	public static CheatDetectionService load(MicroserviceConfig config)
	{
		URLClassLoader loader = new URLClassLoader(new URL[0], null);
		
		AtomicReference<CheatDetectionService> ref = new AtomicReference<>();
		
		Thread loaderThread = new Thread(() ->
		{
			ref.set(new CheatDetectionService(config));
		});
		
		loaderThread.setContextClassLoader(loader);
		loaderThread.start();
		
		try
		{
			loaderThread.join();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		
		return ref.get();
	}
}
