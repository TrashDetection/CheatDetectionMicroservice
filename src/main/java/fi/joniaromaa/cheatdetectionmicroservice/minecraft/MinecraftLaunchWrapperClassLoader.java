package fi.joniaromaa.cheatdetectionmicroservice.minecraft;

import java.lang.reflect.Method;
import java.security.SecureClassLoader;

/**
 * LaunchWrapper uses findClass which is invoked AFTER searching from parent, this is not what we want
 */
public class MinecraftLaunchWrapperClassLoader extends SecureClassLoader
{
	private static Method findClass;
	
	static
	{
		try
		{
			MinecraftLaunchWrapperClassLoader.findClass = ClassLoader.class.getDeclaredMethod("findClass", String.class);
			MinecraftLaunchWrapperClassLoader.findClass.setAccessible(true);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private final ClassLoader loader;
	
	public MinecraftLaunchWrapperClassLoader(ClassLoader loader, ClassLoader parent)
	{
		super(parent);
		
		this.loader = loader;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        synchronized (this.getClassLoadingLock(name))
        {
            Class<?> clazz = this.findLoadedClass(name);
            if (clazz == null)
            {
            	try
            	{
            		return (Class<?>)MinecraftLaunchWrapperClassLoader.findClass.invoke(this.loader);
            	}
            	catch (Throwable e)
            	{
            		return super.loadClass(name, resolve);
            	}
            }

            return clazz;
        }
    }
}
