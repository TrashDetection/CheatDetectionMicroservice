package fi.joniaromaa.cheatdetectionmicroservice.service.loader;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ServiceWhitelistURLClassLoader extends ServiceURLClassLoader
{
	private Set<String> includedPackages;
	private Set<String> includedResources;
	
	public ServiceWhitelistURLClassLoader()
	{
		this(new URL[0]);
	}
	
	public ServiceWhitelistURLClassLoader(URL[] urls)
	{
		super(urls);
		
		this.includedPackages = ConcurrentHashMap.newKeySet();
		this.includedPackages.add("java.");
		
		this.includedResources = ConcurrentHashMap.newKeySet();
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
			
			for(String included : this.includedPackages)
			{
				if (name.startsWith(included))
	        	{
	        		return super.loadClass(name, resolve);
	        	}
			}

			throw new ClassNotFoundException(name);
	    }
    }
	
	@Override
    public URL getResource(String name)
	{
		for(String included : this.includedResources)
		{
			if (name.startsWith(included))
        	{
        		return super.getResource(name);
        	}
		}
		
        return null;
    }
	
	@Override
    public Enumeration<URL> getResources(String name) throws IOException
	{
		for(String included : this.includedResources)
		{
			if (name.startsWith(included))
        	{
        		return super.getResources(name);
        	}
		}
		
		return Collections.emptyEnumeration();
	}
	
    public void addIncludedPackage(String value)
    {
    	this.includedPackages.add(value);
    }
    
    public void addIncludedResources(String value)
    {
    	this.includedResources.add(value);
    }
    
    public static ServiceWhitelistURLClassLoader build(Consumer<ServiceWhitelistURLClassLoader> consumer)
    {
    	ServiceWhitelistURLClassLoader loader = new ServiceWhitelistURLClassLoader();
    	
    	consumer.accept(loader);
    	
    	return loader;
    }
}
