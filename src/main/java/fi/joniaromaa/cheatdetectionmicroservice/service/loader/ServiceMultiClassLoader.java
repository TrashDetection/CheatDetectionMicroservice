package fi.joniaromaa.cheatdetectionmicroservice.service.loader;

import java.io.IOException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class ServiceMultiClassLoader extends SecureClassLoader
{
	private final ClassLoader[] classLoaders;
	
	public ServiceMultiClassLoader(ClassLoader... loaders)
	{
		super(null);
		
		this.classLoaders = loaders;
	}
	
	public ServiceMultiClassLoader(ClassLoader parent, ClassLoader... loaders)
	{
		super(parent);
		
		this.classLoaders = loaders;
	}
	
	@Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
		synchronized (this.getClassLoadingLock(name))
		{
			Class<?> clazz = this.findLoadedClass(name);
			if (clazz != null)
			{
				return clazz;
			}
			
			for(ClassLoader loader : this.classLoaders)
			{
				try
				{
					return loader.loadClass(name);
				}
				catch (ClassNotFoundException e)
				{
				}
			}

			return super.loadClass(name, resolve);
		}
    }

	@Override
    public URL getResource(String name)
	{
		for(ClassLoader loader : this.classLoaders)
		{
			URL resource = loader.getResource(name);
			if (resource != null)
			{
				return resource;
			}
		}
		
        return super.getResource(name);
    }
	
	@Override
    public Enumeration<URL> getResources(String name) throws IOException
	{
		List<URL> resources = new ArrayList<>();
		for(ClassLoader loader : this.classLoaders)
		{
			this.addResources(resources, loader.getResources(name));
		}
		
		this.addResources(resources, super.getResources(name));
		
		return new Vector<URL>(resources).elements();
	}
	
	private void addResources(List<URL> resources, Enumeration<URL> loaderResources)
	{
		while (loaderResources.hasMoreElements())
		{
			resources.add(loaderResources.nextElement());
		}
	}
}
