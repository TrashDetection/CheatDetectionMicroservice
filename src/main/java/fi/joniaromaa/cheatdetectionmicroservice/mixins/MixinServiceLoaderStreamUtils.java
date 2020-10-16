package fi.joniaromaa.cheatdetectionmicroservice.mixins;

/*@Mixin(ServiceLoaderStreamUtils.class)
public abstract class MixinServiceLoaderStreamUtils
{
	@Inject(method = "toList", at = @At("HEAD"), remap = false)
	private static <T> void onToList(ServiceLoader<T> services, CallbackInfoReturnable<List<T>> cir) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field field = ServiceLoader.class.getDeclaredField("loader");
		field.setAccessible(true);
		
		ClassLoader loader = (ClassLoader)field.get(services);

		Field field2 = ClassLoader.class.getDeclaredField("parent");
		field2.setAccessible(true);
		
		MixinServiceLoaderStreamUtils.setFinal(field2, loader, Thread.currentThread().getContextClassLoader());
	}
	

	@Inject(method = "toList", at = @At("RETURN"), remap = false)
	private static <T> void onToList2(ServiceLoader<T> services, CallbackInfoReturnable<List<T>> ci) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, URISyntaxException, IOException
	{
		for(T obj : ci.getReturnValue())
		{
			//ZZZZZzzzzz
			if (!obj.getClass().getName().contains("LanguageProvider"))
			{
				continue;
			}

            try
            {
                FileSystems.newFileSystem(obj.getClass().getProtectionDomain().getCodeSource().getLocation().toURI(), new HashMap<>());
            }
            catch (Throwable ignored)
            {
            }
		}
	}
	
	@ModifyArg(method = "errorHandlingServiceLoader(Ljava/lang/Class;Ljava/util/function/Consumer;)Ljava/util/ServiceLoader;", at = @At(value = "INVOKE"), index = 1, remap = false)
	private static ClassLoader onEerrorHandlingServiceLoader(ClassLoader classLoader)
	{
		return Thread.currentThread().getContextClassLoader();
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