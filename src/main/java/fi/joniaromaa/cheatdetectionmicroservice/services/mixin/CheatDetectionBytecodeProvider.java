package fi.joniaromaa.cheatdetectionmicroservice.services.mixin;

/*public class CheatDetectionBytecodeProvider implements IClassBytecodeProvider
{
	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException
	{
		return this.getClassNode(name, true);
	}

	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException
	{
		System.out.println("BytecodeProvider: " + name);
	       byte[] classBytes;
	        
           URL url = ClassLoader.getSystemClassLoader().getResource(name.replace('.', '/') + ".class");
           if (url == null)
           {
        	   throw new ClassNotFoundException(name.replace('/', '.'));
           }
           
           try
           {
               classBytes = Resources.asByteSource(url).read();
           }
           catch (IOException ioex)
           {
        	   throw new ClassNotFoundException(name.replace('/', '.'));
           }
	        
	        if (classBytes == null)
	        {
	            throw new ClassNotFoundException(name.replace('/', '.'));
	        }

	        ClassNode classNode = new ClassNode();
	        ClassReader classReader = new ClassReader(classBytes);
	        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
	        return classNode;
	}
}
*/