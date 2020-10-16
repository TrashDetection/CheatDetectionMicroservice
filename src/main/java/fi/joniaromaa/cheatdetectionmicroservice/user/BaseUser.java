package fi.joniaromaa.cheatdetectionmicroservice.user;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fi.joniaromaa.cheatdetectionmicroservice.minecraft.MinecraftPacketManager;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.utils.OutputStreamUtils;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPacketsNoOp;
import fi.joniaromaa.minecrafthook.common.IMinecraftHook;
import fi.joniaromaa.minecrafthook.common.MinecraftHooks;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

public abstract class BaseUser
{
	public static final short ZIP_EXTRA_CONTENT_HASH = 0x3000;
	
	@Getter private final int userId;
	@Getter private final int protocolVersion;
	@Getter private final IMinecraftHook hook;
	
	protected int version;
	
	@Getter protected long timeAnalyzed;
	
	protected UserEvaluation evaluation;
	
	protected UserPackets packets;
	
	protected ZipOutputStream outputFile;
	protected PrintWriter writer;
	
	protected BaseUser(int userId, int protocolVersion)
	{
		this.userId = userId;
		this.protocolVersion = protocolVersion;
		this.hook = MinecraftHooks.getHook(protocolVersion);
		
		this.setupPackets();
	}
	
	protected void setupPackets()
	{
		MinecraftPacketManager packetManager = MinecraftPacketManager.getPacketManager(this.protocolVersion, this instanceof InspectUser);
		if (packetManager != null)
		{
			this.packets = new UserPackets(MinecraftPacketManager.getPacketManager(this.protocolVersion, this instanceof InspectUser));
		}
		else
		{
			this.packets = new UserPacketsNoOp();
		}
	}
	
	public void prepare() throws Exception
	{
		this.createLogFile();
		
		this.evaluation.setup();
	}
	
	public void close() throws IOException
	{
		this.evaluation.close();
		
		this.writer.close();
	}
	
	protected void createLogFile() throws IOException
	{
		File file = new File(this.getLogFileFolder(), this.getLogFile().getPath());
		if (file.getParentFile() != null)
		{
			file.getParentFile().mkdirs();
		}

		this.outputFile = new ZipOutputStream(new FileOutputStream(file));
		this.writer = new PrintWriter(new OutputStreamWriter(this.outputFile, StandardCharsets.UTF_8));

		this.writeUserInfo();
	}
	
	private void writeUserInfo() throws IOException
	{
		this.createEntry("userdata.txt", (writer) ->
		{
			writer.println("User Id: " + this.userId);
			writer.println("Protocol: " + this.protocolVersion);
			
			this.writeUserInfoExtra(writer);
		});
	}
	
	public abstract void writeUserInfoExtra(PrintWriter writer);
	
	public void createEntry(String first, IOCallback<PrintWriter> callback) throws IOException
	{
		ZipEntry entry = new ZipEntry(first);
		
		this.outputFile.putNextEntry(entry);
		
		callback.write(this.writer);
		
		this.writer.flush();

		this.outputFile.closeEntry();
	}
	
	public void createEntryRaw(String first, ZipIOCallback<OutputStream> callback) throws IOException
	{
		this.createEntryRaw(new ZipEntry(first), callback);
	}
	
	public void createEntryRaw(String first, ZipIOCallback<OutputStream> callback, String... others) throws IOException
	{
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(first);
        
        for(String string : others)
        {
        	joiner.add(string);
        }
        
		this.createEntryRaw(new ZipEntry(joiner.toString()), callback);
	}
	
	private void createEntryRaw(ZipEntry entry, ZipIOCallback<OutputStream> callback) throws IOException
	{
		this.outputFile.putNextEntry(entry);
		
		callback.write(entry, this.outputFile);
		
		this.outputFile.flush();

		this.outputFile.closeEntry();
	}
	
	public void addIncomingBytes(long hash, ByteBuf buf) throws IOException
	{
		this.createEntryRaw("incoming", (entry, writer) ->
		{
			entry.setExtra(this.makeZipExtraEntryWithHash(hash));
			
			OutputStreamUtils.byteBufToOutputStream(writer, buf);
		}, String.valueOf(this.version));
		
		this.packets.setIncomingBacking(buf);
	}

	public void addOutgoingBytes(long hash, ByteBuf buf) throws IOException
	{
		this.createEntryRaw("outgoing", (entry, writer) ->
		{
			entry.setExtra(this.makeZipExtraEntryWithHash(hash));
			
			OutputStreamUtils.byteBufToOutputStream(writer, buf);
		}, String.valueOf(this.version));
		
		this.packets.setOutgoingBacking(buf);
	}
	
	private byte[] makeZipExtraEntryWithHash(long hash) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(buffer);
		
		output.writeShort(BaseUser.ZIP_EXTRA_CONTENT_HASH); //Header
		output.writeShort(8); //Length
		output.writeLong(hash);
		
		return buffer.toByteArray();
	}
	
	public void onException(Throwable e) throws IOException
	{
		try
		{
			this.createEntry("exception.txt", (writer) ->
			{
				e.printStackTrace(writer);
			});
		}
		catch(Throwable ex)
		{
			throw new RuntimeException(ex);
		}
		
		throw new RuntimeException(e);
	}
	
	public void analyze() throws Exception
	{
		this.evaluation.read(this.packets);
	}
	
	public boolean hasLeftOver()
	{
		return this.packets.hasLeftOver();
	}
	
	public abstract File getLogFileFolder();
	public abstract File getLogFile();
	
	public List<UserViolation> getViolations()
	{
		return this.evaluation.getViolations();
	}
	
	public static interface IOCallback<T>
	{
		public void write(T object) throws IOException;
	}
	
	public static interface ZipIOCallback<T>
	{
		public void write(ZipEntry entry, T object) throws IOException;
	}
	
	@Override
	public String toString()
	{
		return String.format("Base User | Id: %d | Protocol: %d", this.userId, this.protocolVersion);
	}
}
