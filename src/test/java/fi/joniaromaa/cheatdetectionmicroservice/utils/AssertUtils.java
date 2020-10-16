package fi.joniaromaa.cheatdetectionmicroservice.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

public class AssertUtils
{
	public static void assertEmpty(List<?> list)
	{
		assertTrue(list.isEmpty());
	}
	
	public static <T> void assetEquals(List<T> expected, List<T> actual)
	{
		assertEquals(actual.toString(), expected.size(), actual.size());
		
		for(int i = 0; i < expected.size(); i++)
		{
			assertEquals(expected.get(i), actual.get(i));
		}
	}
}
