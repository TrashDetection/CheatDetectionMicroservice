package fi.joniaromaa.cheatdetectionmicroservice.services.mixin;

/*public class CheatDetectionClassProvider implements IClassProvider
{
    @Override
    @Deprecated
    public URL[] getClassPath()
    {
    	System.out.println("get class apth");
    	return null;
        //return Java9ClassLoaderUtil.getSystemClassPathURLs();
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException
    {
    	System.out.println("findClass: " + name);
        return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException
    {
    	System.out.println("findClass: " + name);
        return Class.forName(name, initialize, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException
    {
    	System.out.println("findAgent");
    	return Class.forName(name, initialize, ClassLoader.getSystemClassLoader());
    }
}
*/