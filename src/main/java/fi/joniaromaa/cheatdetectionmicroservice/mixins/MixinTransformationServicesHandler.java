package fi.joniaromaa.cheatdetectionmicroservice.mixins;

/*@Mixin(targets = "cpw.mods.modlauncher.TransformationServicesHandler")
public abstract class MixinTransformationServicesHandler
{	
	@Redirect(method = "discoverServices", at = @At(value = "NEW", args = "class=cpw/mods/modlauncher/TransformationServicesHandler$TransformerClassLoader"))
	private @Coerce Object onDiscoverServices(URL[] urls) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException
	{
		Class<?> clazz = Class.forName("cpw.mods.modlauncher.TransformationServicesHandler$TransformerClassLoader");
		
		Constructor<?> constructor = clazz.getDeclaredConstructor(URL[].class);
		constructor.setAccessible(true);
		
		URLClassLoader contextClassLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
		
		Object classLoader = constructor.newInstance(new Object[]
		{
			contextClassLoader.getURLs()
		});
		
		Field field = ClassLoader.class.getDeclaredField("parent");
		field.setAccessible(true);
		
		MixinTransformationServicesHandler.setFinal(field, classLoader, contextClassLoader);
		
		return classLoader;
	}
	
	private static void setFinal(Field field, Object target, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(target, newValue);
	}
}
*/