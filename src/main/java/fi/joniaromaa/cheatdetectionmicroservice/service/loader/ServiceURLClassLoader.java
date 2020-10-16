package fi.joniaromaa.cheatdetectionmicroservice.service.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ServiceURLClassLoader extends URLClassLoader
{
	private Set<String> exludedPackages;

	private Map<String, Class<?>> preCachedClasses;
	
	public ServiceURLClassLoader(URL[] urls)
	{
		this(urls, ClassLoader.getSystemClassLoader());
	}
	
	public ServiceURLClassLoader(ClassLoader parent)
	{
		this(new URL[0], parent);
	}
	
	public ServiceURLClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);

		this.exludedPackages = ConcurrentHashMap.newKeySet();
		this.exludedPackages.add("java.");
		this.exludedPackages.add("sun.");
		
		this.preCachedClasses = new ConcurrentHashMap<>();
	}

    static
    {
        ClassLoader.registerAsParallelCapable();
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
			
			for(String exluded : this.exludedPackages)
			{
				if (name.startsWith(exluded))
	        	{
	        		return super.loadClass(name, resolve);
	        	}
			}
        	
    		clazz = this.preCachedClasses.get(name);
    		if (clazz != null)
    		{
    			return clazz;
    		}

			return this.loadClass0(name, resolve);
	    }
    }
	
	protected final Class<?> loadClass0(String name, boolean resolve) throws ClassNotFoundException
	{
    	try
    	{
    		URL classResource = this.getClassResource(name);
    		if (classResource == null)
    		{
    			return super.loadClass(name, resolve);
    		}

			byte[] classBytes = this.getClassBytes(classResource);
    		
            CodeSource codeSource = this.getCodeSource(name, classResource);
            
			return this.defineClass(name, classBytes, 0, classBytes.length, codeSource);
		}
    	catch (Throwable e)
    	{
			throw new ClassNotFoundException(name, e);
		}
	}
	
    protected byte[] getClassBytes(URL classResource) throws IOException
    {
    	try(InputStream stream = classResource.openStream())
    	{
            return this.getBytes(stream);
    	}
    }
    
    protected final byte[] getBytes(InputStream stream) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream(stream.available());

        byte[] buffer = new byte[1024];
        
        int n = 0;
        while ((n = stream.read(buffer, 0, buffer.length)) != -1)
        {
        	output.write(buffer, 0, n);
        }
        
        return output.toByteArray();
    }
    
    protected final String getClassFile(String className)
    {
    	return className.replace('.', '/').concat(".class");
    }

    protected final URL getClassResource(String name)
    {
        String resourcePath = this.getClassFile(name);
        
        return this.getResource(resourcePath);
    }
    
    private CodeSource getCodeSource(String className, URL classResource) throws IOException, URISyntaxException
    {
        int lastDot = className.lastIndexOf('.');
        String packageName = lastDot == -1 ? "" : className.substring(0, lastDot);
        
        CodeSigner[] signers = null;
        
    	URLConnection urlConnection = classResource.openConnection();
        if (urlConnection instanceof JarURLConnection)
        {
            JarURLConnection jarUrlConnection = (JarURLConnection)urlConnection;
            
            JarFile jarFile = jarUrlConnection.getJarFile();
            if (jarFile != null)
            {
                JarEntry entry = jarFile.getJarEntry(this.getClassFile(className));
                Manifest manifest = jarFile.getManifest();

                signers = entry.getCodeSigners();
                
                Package pkg = this.getPackage(packageName);
                if (pkg == null)
                {
                    pkg = this.definePackage(packageName, manifest, jarUrlConnection.getJarFileURL());
                }
            }

            return new CodeSource(jarUrlConnection.getJarFileURL(), signers);
        }

        return new CodeSource(urlConnection.getURL(), signers);
    }
    
    public void addExludedPackage(String value)
    {
    	this.exludedPackages.add(value);
    }
    
    public void addCachedClass(Class<?> value)
    {
    	this.preCachedClasses.put(value.getName(), value);
    }
    
    public static ServiceURLClassLoader build(ClassLoader parent, Consumer<ServiceURLClassLoader> consumer)
    {
    	ServiceURLClassLoader loader = new ServiceURLClassLoader(parent);
    	
    	consumer.accept(loader);
    	
    	return loader;
    }
}
