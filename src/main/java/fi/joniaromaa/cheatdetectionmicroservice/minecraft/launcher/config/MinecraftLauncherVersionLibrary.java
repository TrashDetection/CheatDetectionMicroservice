package fi.joniaromaa.cheatdetectionmicroservice.minecraft.launcher.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class MinecraftLauncherVersionLibrary
{
	@Getter private Download downloads;

	@Getter private String name;
	@Getter private URL url;
	
	@Getter private Map<String, String> natives;
	
	@Getter private List<Rule> rules;
	
	public Path getFile(Path root)
	{
		String[] groups = this.name.split(":");
		
		String[] group = groups[0].split("\\.");
		for(String part : group)
		{
			root = root.resolve(part);
		}
		
		String artifactId = groups[1];
		String version = groups[2];
		
		return root.resolve(artifactId)
				.resolve(version)
				.resolve(artifactId + "-" + version + ".jar");
	}
	
	public URL getDownloadUrl() throws MalformedURLException
	{
		if (this.getUrl() != null)
		{
			return this.getDownloadUrl0(this.getUrl());
		}
		
		return null;
	}
	
	private URL getDownloadUrl0(URL root) throws MalformedURLException
	{
		StringBuilder sb = new StringBuilder();
		
		String[] groups = this.name.split(":");
		
		String[] group = groups[0].split("\\.");
		for(String part : group)
		{
			sb.append(part).append('/');
		}
		
		String artifactId = groups[1];
		String version = groups[2];
		
		return new URL(root, sb.append(artifactId).append('/')
				.append(version).append('/')
				.append(artifactId + "-" + version + ".jar")
				.toString());
	}
	
	public static class Download
	{
		@Getter private File artifact;
		
		@Getter private Map<String, File> classifiers;
		
		public static class File
		{
			@Getter private String path;
			@Getter private String sha1;
			@Getter private URL url;
			
			@Getter private int size;
		}
	}
	
	public static class Rule
	{
		@Getter private String action;
		@Getter private Map<String, String> os;
	}
}
