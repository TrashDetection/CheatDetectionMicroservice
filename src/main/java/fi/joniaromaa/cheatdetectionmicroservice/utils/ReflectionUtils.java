package fi.joniaromaa.cheatdetectionmicroservice.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionUtils
{
	public static void setFinal(Field field, Object target, Object newValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		field.set(target, newValue);
	}
}
