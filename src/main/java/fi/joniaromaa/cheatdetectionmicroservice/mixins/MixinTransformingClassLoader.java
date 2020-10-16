package fi.joniaromaa.cheatdetectionmicroservice.mixins;

/*@Mixin(TransformingClassLoader.class)
public abstract class MixinTransformingClassLoader
{
	@Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void onConstructor(CallbackInfo ci) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field field = ClassLoader.class.getDeclaredField("parent");
		field.setAccessible(true);
		
		MixinTransformingClassLoader.setFinal(field, this, Thread.currentThread().getContextClassLoader());
    }

	@Inject(method = "loadClass", at = @At("HEAD"), remap = false)
	private void a(String name, boolean resolve, CallbackInfoReturnable ci)
	{
		//System.out.println(name);
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