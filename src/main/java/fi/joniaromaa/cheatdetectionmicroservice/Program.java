package fi.joniaromaa.cheatdetectionmicroservice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fi.joniaromaa.cheatdetectionmicroservice.service.ServiceLibraryLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.cheatdetection.ICheatDetectionService;
import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceMultiClassLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceURLClassLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.loader.ServiceWhitelistURLClassLoader;
import fi.joniaromaa.cheatdetectionmicroservice.service.minecraft.IMinecraftHookService;

public class Program
{
	private static final Logger LOGGER = LogManager.getLogger(Program.class);
	
	private static ICheatDetectionService service;
	
	static
	{
		//Due to our class loading hell we need to make sure jmx
		System.setProperty("log4j2.disable.jmx", "true");
	}
	
	public static void main(String[] args) throws Throwable
	{
		Program.LOGGER.info("Booting up, searching for services..");

		ServiceURLClassLoader minecraftHookClassLoader = ServiceWhitelistURLClassLoader.build((l) ->
		{
			l.addIncludedPackage("fi.joniaromaa.cheatdetectionmicroservice.service.minecraft.");
			l.addIncludedPackage("fi.joniaromaa.cheatdetectionmicroservice.services.minecraft.");
			
			l.addIncludedPackage("fi.joniaromaa.minecrafthook.common.");
			
			l.addIncludedResources("META-INF/services/fi.joniaromaa.cheatdetectionmicroservice.service.minecraft.IMinecraftHookService");
			l.addIncludedResources("fi/joniaromaa/minecrafthook/common/");
		});
		
		ServiceURLClassLoader cheatDetectionClassLoader = ServiceURLClassLoader.build(new ServiceMultiClassLoader(ClassLoader.getSystemClassLoader(), new ClassLoader[]
		{
			minecraftHookClassLoader
		}), (l) ->
		{
			l.addExludedPackage("fi.joniaromaa.minecrafthook.common.");
			l.addExludedPackage("org.apache."); //This should be safe here, right?
		});

		ServiceLibraryLoader.load(IMinecraftHookService.class, minecraftHookClassLoader, (s) -> {});
		ServiceLibraryLoader.load(ICheatDetectionService.class, cheatDetectionClassLoader, (s) -> Program.service = s);
		
		Thread.currentThread().setContextClassLoader(cheatDetectionClassLoader);
		
		Program.service.load();
		
		Program.LOGGER.info("Ready!");
		
		try (Scanner scanner = new Scanner(System.in))
		{
			while (true)
			{
				try
				{
					if (scanner.hasNextLine())
					{
						String line = scanner.nextLine();
						String[] cmdArgs = line.split(" ");
						
						Program.parseCommand(scanner, cmdArgs);
					}
				}
				catch(InterruptedException e)
				{
					Thread.currentThread().interrupt();
					
					break;
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void parseCommand(Scanner scanner, String[] args) throws Exception
	{
		switch (args[0])
		{
			case "classloader":
			{
				for(Class<?> clazz : getLoadedClasses(ClassLoader.getSystemClassLoader()))
				{
					System.out.println("LOADED: " + clazz);
				}
			}
			break;
			case "shutdown":
			{
				System.out.println("Shutdown...");
				
				Program.service.shutdown();
				
				throw new InterruptedException();
			}
			default:
				System.out.println("Boo");
				break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<Class<?>> getLoadedClasses(ClassLoader loader) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Field field = ClassLoader.class.getDeclaredField("classes");
		field.setAccessible(true);

		Vector<Class<?>> classes = (Vector<Class<?>>)field.get(loader);
		
		return new ArrayList<Class<?>>(classes);
	}
	
	/*public static void setSeucityManagerToNull()
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>()
	    {
			@Override
			public Object run()
			{
				System.setSecurityManager(null);
				
				return null;
			}
	    });
	}
	
	private static void parseCommand(Scanner scanner, String[] args) throws Exception
	{
		switch (args[0])
		{
			case "test":
			{
				if (args.length != 2)
				{
					System.out.println("Usage: test [file]");
					
					return;
				}
				
				Program.inspectString(args[1], false);
			}
			break;
			case "inspect":
			{
				if (args.length != 2)
				{
					System.out.println("Usage: inspect [file]");
					
					return;
				}
				
				Program.inspectString(args[1], true);
			}
			break;
			case "banall":
			{
				System.out.println("Processing banall");
				
				Program.microservice.getService().getServerConnection().getBanManager().scheduledBans(true);
			}
			break;
			case "showbans":
			{
				Program.microservice.getService().getServerConnection().getBanManager().showBans();
			}
			break;
			case "removeban":
			{
				if (args.length != 2)
				{
					System.out.println("Usage: removeban [banId]");
					
					return;
				}
				
				System.out.println("Removing...");
				
				int banId = Integer.parseInt(args[1]);
				
				Program.microservice.getService().getServerConnection().getBanManager().markBanResolved(banId);
			}
			break;
			case "entitymovement":
			{
				if (args.length > 2)
				{
					System.out.println("Usage: entitymovement <file>");
					
					return;
				}
				
				if (args.length == 2)
				{
					File file = new File(args[1]);
					if (!file.exists())
					{
						System.out.println("Unable to find the file");
						
						return;
					}
					
					if (!file.isFile())
					{
						System.out.println("That isn't a file");
						
						return;
					}
					
					try(Scanner fileScanner = new Scanner(file))
					{
						Program.entityMovement(fileScanner);
					}
				}
				else
				{
					Program.entityMovement(scanner);
				}
			}
			break;
			case "trackmovement":
			{
				if (args.length != 5)
				{
					System.out.println("Usage: trackmovement [file] [entityId] [start] [end]");
					
					return;
				}
				
				File file = new File(args[1]);
				if (!file.exists())
				{
					System.out.println("Unable to find the file");
					
					return;
				}
				
				if (!file.isFile())
				{
					System.out.println("That isn't a file");
					
					return;
				}
				
				int entityId = Integer.parseInt(args[2]);
				int start = Integer.parseInt(args[3]);
				int end = Integer.parseInt(args[4]);

				throw new RuntimeException("Refactor");
				
				InspectUser user = Program.getInspectData(file);
				
				ChunkedByteArray newArray = new ChunkedByteArray();
				
				ChunkedByteArrayBufferReader<MinecraftOutgoingPacket> reader = new ChunkedByteArrayBufferReader<MinecraftOutgoingPacket>()
				{
					private final AbstractMinecraftPacketManager packetManager = MinecraftPacketManagers.getPacketManager(user.getProtocolVersion(), true);
					private final AtomicInteger i = new AtomicInteger(1);
					
					@Override
					protected void read0(ByteBuf buffer)
					{
						if (this.packetManager == null)
						{
							return;
						}
						
						BufferReader reader = new BufferReader(null);
						
						while (buffer.isReadable())
						{
							ByteBuf startBuf = buffer.retainedSlice();
							
							int length = ByteBufUtils.readVarInt(buffer);

							reader.setBuffer(buffer.readRetainedSlice(length));
							
							try
							{
								MinecraftOutgoingPacket p = this.packetManager.readOutgoingPacket(reader);
								if (p instanceof AbstractConfirmTransactionOutgoingPacket)
								{
									AbstractConfirmTransactionOutgoingPacket packet = (AbstractConfirmTransactionOutgoingPacket)p;
									if (packet.getWindowId() == AbstractClickWindowIncomingPacket.PLAYER_INVENTORY_ID && packet.getActionNumber() == ConfirmationModule.CONFIRM_TRANSACTION_PREV_ID)
									{
										i.incrementAndGet();
									}
								}
								else if (p instanceof AbstractSpawnPlayerOutgoingPacket)
								{
									AbstractSpawnPlayerOutgoingPacket packet = (AbstractSpawnPlayerOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityOutgoingPacket)
								{
									AbstractEntityOutgoingPacket packet = (AbstractEntityOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityTeleportOutgoingPacket)
								{
									AbstractEntityTeleportOutgoingPacket packet = (AbstractEntityTeleportOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityHeadLookOutgoingPacket)
								{
									AbstractEntityHeadLookOutgoingPacket packet = (AbstractEntityHeadLookOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityMetadataOutgoingPacket)
								{
									AbstractEntityMetadataOutgoingPacket packet = (AbstractEntityMetadataOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityStatusOutgoingPacket)
								{
									AbstractEntityStatusOutgoingPacket packet = (AbstractEntityStatusOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractEntityPropertiesOutgoingPacket)
								{
									AbstractEntityPropertiesOutgoingPacket packet = (AbstractEntityPropertiesOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractUseBedOutgoingPacket)
								{
									AbstractUseBedOutgoingPacket packet = (AbstractUseBedOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
								else if (p instanceof AbstractAnimationOutgoingPacket)
								{
									AbstractAnimationOutgoingPacket packet = (AbstractAnimationOutgoingPacket)p;
									if (packet.getEntityId() == entityId && i.get() >= start && i.get() <= end)
									{
										newArray.write(startBuf.readSlice(length + varintSize(length)));
									}
								}
							}
							finally
							{
								startBuf.release();
								
								reader.getBuffer().release();
							}
						}
					}
				};
				
				reader.read(user.getOutgoingBytes());

				File outputFile = new File("trackmovement", user.getUserId() + "-" + entityId + "-" + start + "-" + end + ".zip");
				outputFile.getParentFile().mkdirs();
				
				try (ZipOutputStream zippedDetails = new ZipOutputStream(new FileOutputStream(outputFile)))
				{
					ZipEntry outgoingData = new ZipEntry("outgoing.data");
					zippedDetails.putNextEntry(outgoingData);
					OutputStreamUtils.chunkedByteArrayToStream(zippedDetails, newArray);
					zippedDetails.closeEntry();
				}
				
				System.out.println("Completed!");
			}
			//break;
			case "savedata":
			{
				if (args.length != 3)
				{
					System.out.println("Usage: savedata [serverId] [userId]");
					
					return;
				}
			
				int serverId = Integer.parseInt(args[1]);
				int userId = Integer.parseInt(args[2]);
				
				Program.microservice.getService().getServerConnection().saveData(serverId, userId);
			}
			break;
			case "shutdown":
			{
				System.out.println("Shutdown...");
				
				Program.microservice.shutdown();
				
				throw new InterruptedException();
			}
			default:
				System.out.println("Boo");
				break;
		}
	}

    private static int varintSize(int paramInt)
    {
        if ((paramInt & 0xFFFFFF80) == 0)
        {
            return 1;
        }
        else if (( paramInt & 0xFFFFC000) == 0)
        {
            return 2;
        }
        else if (( paramInt & 0xFFE00000) == 0)
        {
            return 3;
        }
        else if (( paramInt & 0xF0000000) == 0)
        {
            return 4;
        }
        
        return 5;
    }
	
	private static void entityMovement(Scanner scanner)
	{
		System.out.println("Welcome to the entity movement mess, first, outgoing");
		
		final int ENTITY_ID = 1;
		
		PlayerInteractWithEntityModule module = null; // new PlayerInteractWithEntityModule(null, new InspectUser(0, 0, 0, false));
		module.analyzeOutgoing(new ISpawnPlayerOutgoingPacket()
		{
			@Override
			public int getEntityId()
			{
				return ENTITY_ID;
			}

			@Override
			public int getServerX() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getServerY() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getServerZ() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getX() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getY() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getZ() {
				// TODO Auto-generated method stub
				return 0;
			}
		});
		
		module.analyzeOutgoing(IConfirmTransactionOutgoingPacket.newConfirmInstance());
		
		while (true)
		{
			try
			{
				if (scanner.hasNextLine())
				{
					String line = scanner.nextLine().trim();
					if (line.isEmpty())
					{
						continue;
					}
					
					if (line.equals("done"))
					{
						System.out.println("Done!");
						
						break;
					}
					
					boolean done = true;

					String[] cmdArgs = line.split(" ");
					switch(cmdArgs[0])
					{
						case "tp":
						{
							int serverX = Integer.parseInt(cmdArgs[1]);
							int serverY = Integer.parseInt(cmdArgs[2]);
							int serverZ = Integer.parseInt(cmdArgs[3]);
							
							module.analyzeOutgoing(IEntityTeleportOutgoingPacket.newInstance(ENTITY_ID, serverX, serverY, serverZ));
						}
						break;
						case "move":
						{
							int serverX = Integer.parseInt(cmdArgs[1]);
							int serverY = Integer.parseInt(cmdArgs[2]);
							int serverZ = Integer.parseInt(cmdArgs[3]);
							
							module.analyzeOutgoing(IEntityOutgoingPacket.newInstance(ENTITY_ID, serverX, serverY, serverZ));
						}
						break;
						default:
						{
							done = false;
							
							System.out.println("Whats that...");
						}
						break;
					}
					
					if (!done)
					{
						continue;
					}
					
					module.analyzeOutgoing(IConfirmTransactionOutgoingPacket.newConfirmInstance());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		module.analyzeIncoming(IConfirmTransactionIncomingPacket.newConfirmInstance());
		module.analyzeIncoming(IPlayerIncomingPacket.ground()); //So we are "in" game
		
		Entity entity = module.getEntity(ENTITY_ID);
		
		System.out.println("Now the incomings...");
		
		while (true)
		{
			try
			{
				if (scanner.hasNextLine())
				{
					String line = scanner.nextLine().trim();
					if (line.isEmpty())
					{
						continue;
					}
					
					if (line.equals("done"))
					{
						System.out.println("Done!");
						
						break;
					}

					String[] cmdArgs = line.split(" ");
					switch(cmdArgs[0])
					{
						case "confirm":
							module.analyzeIncoming(IConfirmTransactionIncomingPacket.newConfirmInstance());
							System.out.println("Confirm");
							break;
						case "step":
							module.analyzeIncoming(IPlayerIncomingPacket.ground());
							
							System.out.println("Entity now at: " + entity.getX() + " | " + entity.getY() + " | " + entity.getZ() + " | ");
							
							if (entity.getMoveToIncrements() == 0)
							{
								System.out.println("Entity came to full stop");
							}
							break;
						default:
						{
							System.out.println("Whats that...");
						}
						break;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private static void inspectString(String path, boolean inspect) throws ZipException, IOException
	{
		File file = new File(path);
		if (!file.exists())
		{
			System.out.println("File " + file + " not found");
			
			return;
		}
		
		if (file.isFile())
		{
			Program.inspectFile(file, inspect);
		}
		else if (file.isDirectory())
		{
			System.out.println("Starting evaluation on a folder " + file.getName() + " ...");
			
			try (Stream<Path> paths = Files.walk(file.toPath()))
			{
				paths.sorted(Comparator.comparing(p -> p.toFile().length())).collect(Collectors.toList()).parallelStream().filter(Files::isRegularFile).forEachOrdered((f) ->
				{
					try
					{
						Program.inspectFile(f.toFile(), inspect);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				});
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			System.out.println("Evaluation done on a folder " + file.getName() + "!");
		}
	}
	
	private static Long getHash(ZipEntry entry) throws IOException
	{
		byte[] extra = entry.getExtra();
		if (extra == null)
		{
			return null;
		}
		
		ByteArrayInputStream buffer = new ByteArrayInputStream(extra);
		DataInputStream input = new DataInputStream(buffer);
		
		while(input.available() > 0)
		{
			short tag = input.readShort();
			short length = input.readShort();
			
			switch(tag)
			{
				case BaseUser.ZIP_EXTRA_CONTENT_HASH:
					return input.readLong();
				default:
					input.skipBytes(length);
					break;
			}
		}
		
		return null;
	}
	
	private static void inspectFile(File file, boolean inspect) throws ZipException, IOException
	{
    	System.out.println("Reading the log file " + file.getName());
    	
		Program.getInspectData(file, inspect, (data) ->
		{
	    	System.out.println("Starting evaluation on the file " + file.getName());

	    	try
	    	{
	    		data.user.prepare();
	    		
	    		long baseHash = (long)data.user.getUserId() | (long)data.user.getSessionId() << 32;
	    		
	    		StreamingXXHash64 incomingHash = Program.xxHashFactory.newStreamingHash64(baseHash);
	    		StreamingXXHash64 outgoingHash = Program.xxHashFactory.newStreamingHash64(baseHash);
	    		
	    		//Read in 64kb chunks, that seems fair to consider these can contain a shit ton of packets
		    	byte[] buffer = new byte[1024 * 64];
		    	
		    	//First we read outgoing packets a bit, then switch to incoming so we can handle packets that can already be dealt with to reduce memory pressure
		    	//Then we start over again until we have read everything
		    	do
		    	{
		    		Long zipIncomingHash = Program.getHash(data.incomingEntry);
		    		Long zipOutgoingHash = Program.getHash(data.outgoingEntry);
		    		
		    		incomingHash.reset();
		    		outgoingHash.reset();
		    		
		    		while (true)
		    		{
			    		boolean hasOutgoing = true;
			    		
			    		int amount = data.outgoing.read(buffer);
			    		if (amount > 0)
			    		{
			    			outgoingHash.update(buffer, 0, amount);
			    			
			    			data.user.addOutgoingBytes(0, Unpooled.wrappedBuffer(buffer, 0, amount));
				    		data.user.analyze();
			    		}
			    		else
			    		{
			    			hasOutgoing = false;
			    		}
			    		
			    		amount = data.incoming.read(buffer);
			    		if (amount > 0)
			    		{
			    			incomingHash.update(buffer, 0, amount);
			    			
			    			data.user.addIncomingBytes(0, Unpooled.wrappedBuffer(buffer, 0, amount));
				    		data.user.analyze();
			    		}
			    		else if (!hasOutgoing)
			    		{
			    			break;
			    		}
			    		
			    		data.user.versionBump();
		    		}
		    		
		    		if (zipIncomingHash != null && !zipIncomingHash.equals(incomingHash.getValue()))
		    		{
		    			throw new RuntimeException("Incoming hash no match");
		    		}
		    		
		    		if (zipOutgoingHash != null && !zipOutgoingHash.equals(outgoingHash.getValue()))
		    		{
		    			throw new RuntimeException("Outgoing hash no match");
		    		}
		    	} while(data.hasMoreIncoming() | data.hasMoreOutgoing());
	    	}
	    	catch(Exception e)
	    	{
	    		throw new RuntimeException(e);
	    	}
	    	
	    	try {
				data.user.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	System.out.println("Evaluation done on the file " + file.getName());
	    	
	    	System.out.println(data.user.getViolations());
	    	
	    	if (data.user.hasLeftOver())
	    	{
	    		System.out.println("HAS LEFT OVER!!!");
	    	}
		});
		
	    try(ZipFile zipFile = new ZipFile(file))
	    {
	    	ZipEntry violations = zipFile.getEntry("violations.txt");
	    	if (violations != null)
	    	{
	    		int violationsCount = 0;

		    	byte[] buffer = new byte[1024];
		    	try (InputStream input = zipFile.getInputStream(violations))
		    	{
		    		while (true)
		    		{
			    		int amount = input.read(buffer);
			    		if (amount > 0)
			    		{
			    			byte[] bytes = new byte[amount];
			    			
			    			System.arraycopy(buffer, 0, bytes, 0, amount);
			    			
			                for (int i = 0; i < amount; i++)
			                {
			                    if (bytes[i] == '\n')
			                    {
			                    	violationsCount++;
			                    }
			                }
			    		}
			    		else
			    		{
			    			break;
			    		}
		    		}
		    	}
		    	
		    	//if (violationsCount != user.getViolations().size())
		    	//{
		    	//	System.out.println("User " + user.getUserId() + " violation count changed! " + violationsCount + " -> " + user.getViolations().size());
		    	//}
	    	}
		}
	    catch (IOException e)
	    {
			e.printStackTrace();
		}
	}
	
	private static void getInspectData(File file, boolean inspect, Consumer<InspectData> callback) throws ZipException, IOException
	{
		try(ZipFile zipFile = new ZipFile(file))
	    {
	    	ZipEntry userdataEntry = zipFile.getEntry("userdata.txt");
	    	if (userdataEntry == null)
	    	{
	    		throw new RuntimeException("The file is missing userdata.txt");
	    	}
	    	
	    	int userId = -1;
	    	int sessionId = -1;
	    	int protocolVersion = -1;
	    	long timeAnalyzed = -1;

	    	try (InputStream input = zipFile.getInputStream(userdataEntry); Scanner reader = new Scanner(input))
	    	{
	    		while (reader.hasNextLine())
	    		{
		    		String line = reader.nextLine();

		    		String[] data = line.trim().split("\\s*:\\s*", 2);
		    		if (data[0].equalsIgnoreCase("User Id"))
		    		{
		    			userId = Integer.parseInt(data[1]);
		    		}
		    		else if (data[0].equalsIgnoreCase("Session Id"))
		    		{
		    			sessionId = Integer.parseInt(data[1]);
		    		}
		    		else if (data[0].equalsIgnoreCase("Protocol"))
		    		{
		    			protocolVersion = Integer.parseInt(data[1]);
		    		}
		    		else if (data[0].equalsIgnoreCase("Time Analyzed"))
		    		{
		    			timeAnalyzed = Long.parseLong(data[1]);
		    		}
		    		else if (data[0].equalsIgnoreCase("RE-EVALUATION"))
		    		{
		    			throw new RuntimeException("This file is re-evaluation and not recommended for study, use the original file instead");
		    		}
	    		}
	    	}

	    	InspectUser user = new InspectUser(sessionId, userId, protocolVersion, timeAnalyzed, inspect);
    	
    		InspectData data = new InspectData(user, zipFile);
    		data.findIncoming();
    		data.findOutgoing();
    		
	    	callback.accept(data);
		}
	}
	
	private static class InspectData
	{
		private final InspectUser user;
		
		private final ZipFile zipFile;
		
		private int incomingIndex;
		private int outgoingIndex;
		
		private ZipEntry incomingEntry;
		private ZipEntry outgoingEntry;
		
		private InputStream incoming;
		private InputStream outgoing;
		
		public InspectData(InspectUser user, ZipFile zipFile)
		{
			this.user = user;
			
			this.zipFile = zipFile;
		}
		
		public void findIncoming() throws IOException
		{
			this.incomingEntry = this.zipFile.getEntry("incoming.data");
	    	if (this.incomingEntry == null)
	    	{
	    		this.incomingEntry = this.zipFile.getEntry("incoming/0");
	    		if (this.incomingEntry == null)
	    		{
	    			throw new RuntimeException("There was no incoming data");
	    		}
	    	}
	    	
	    	if (!this.incomingEntry.getName().contains("/"))
	    	{
	    		this.incomingIndex = -1;
	    		this.incoming = this.zipFile.getInputStream(this.incomingEntry);
	    	}
	    	else
	    	{
	    		this.hasMoreIncoming();
	    	}
		}
		
		public void findOutgoing() throws IOException
		{
	    	this.outgoingEntry = this.zipFile.getEntry("outgoing.data");
	    	if (this.outgoingEntry == null)
	    	{
	    		this.outgoingEntry = this.zipFile.getEntry("outgoing/0");
	    		if (this.outgoingEntry == null)
	    		{
	    			throw new RuntimeException("There was no outgoing data");
	    		}
	    	}

	    	if (!this.outgoingEntry.getName().contains("/"))
	    	{
	    		this.outgoingIndex = -1;
	    		this.outgoing = this.zipFile.getInputStream(this.outgoingEntry);
	    	}
	    	else
	    	{
	    		this.hasMoreOutgoing();
	    	}
		}
		
		public boolean hasMoreIncoming() throws IOException
		{
			if (this.incomingIndex == -1)
			{
				return false;
			}
			
    		this.incomingEntry = this.zipFile.getEntry("incoming/" + this.incomingIndex++);
    		if (this.incomingEntry == null)
    		{
    			this.incoming = null;
    			
    			return false;
    		}
    		
    		this.incoming = this.zipFile.getInputStream(this.incomingEntry);
    		
    		return true;
		}
		
		public boolean hasMoreOutgoing() throws IOException
		{
			if (this.outgoingIndex == -1)
			{
				return false;
			}
			
    		this.outgoingEntry = this.zipFile.getEntry("outgoing/" + this.outgoingIndex++);
    		if (this.outgoingEntry == null)
    		{
    			this.incoming = null;
    			
    			return false;
    		}
    		
    		this.outgoing = this.zipFile.getInputStream(this.outgoingEntry);
    		
    		return true;
		}
	}*/
}
