package fi.joniaromaa.cheatdetectionmicroservice.utils;

import lombok.Getter;

public final class OsUtils
{
	@Getter private final static String os = OsUtils.getOs0();
	   
	private static String getOs0()
	{
		String os = System.getProperty("os.name");
		if (os.contains("Windows"))
		{
			return "windows";
		}
		
		throw new RuntimeException("Unsupported OS version: " + os);
	}
}
