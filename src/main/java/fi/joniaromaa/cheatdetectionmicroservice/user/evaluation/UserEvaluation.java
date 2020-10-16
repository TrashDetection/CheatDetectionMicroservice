package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fi.joniaromaa.cheatdetectionmicroservice.user.BaseUser;
import fi.joniaromaa.cheatdetectionmicroservice.user.BaseUser.IOCallback;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.GameStatusAwareSubmodule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.Module;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.inspect.DebugModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.inspect.ForceSaveModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action.BlockBreakModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action.EntityActionTooFastModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.action.TooManyInteractsModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.combat.PlayerAttackNoSwingModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.combat.PlayerInteractWithEntityModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory.HeldItemNotChangedModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory.InvalidContainerTransactionIdModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory.InvalidInventorySlotChangeModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.inventory.PlayerInventoryNotOpenModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.InvalidPlayerLookModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.NoPositionUpdateModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.PlayerInterackStatusModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.PlayerSpeedModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.SprintSneakingModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.TooManyMovePacketsModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.player.movement.UserNoUpdateModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.world.BlockDigNoSwingModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.world.BlockPlaceNoSwingModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules.world.InvalidBlockPositionModule;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;
import fi.joniaromaa.cheatdetectionmicroservice.utils.UserPackets;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IPluginMessageIncomingPacket;
import lombok.Getter;
import lombok.Setter;

public class UserEvaluation
{
	private static final Logger LOGGER = LogManager.getLogger(UserEvaluation.class);
	
	protected static final Set<Class<? extends Module>> DEFAULT_MODULES = new HashSet<>(Arrays.asList(
			//Inspect
			DebugModule.class,
			ForceSaveModule.class,
			
			//Player -> Action
			BlockBreakModule.class,
			EntityActionTooFastModule.class,
			TooManyInteractsModule.class,
			
			//Player -> Combat
			PlayerAttackNoSwingModule.class,
			PlayerInteractWithEntityModule.getRelevantClass(),

			//Player -> Inventory
		    HeldItemNotChangedModule.class,
		    InvalidContainerTransactionIdModule.class,
		    InvalidInventorySlotChangeModule.class, //TODO
		    PlayerInventoryNotOpenModule.class,
		    
		    //Player -> Movement
		    InvalidPlayerLookModule.class,
		    NoPositionUpdateModule.class,
		    PlayerInterackStatusModule.class,
		    PlayerSpeedModule.class,
		    SprintSneakingModule.class,
		    TooManyMovePacketsModule.class,
		    UserNoUpdateModule.class, //TODO: Impossible?
		    
		    //World
		    BlockDigNoSwingModule.class,
		    BlockPlaceNoSwingModule.class,
		    InvalidBlockPositionModule.class
    ));
	
	@Getter private final BaseUser user;
	
	private Module[] modulesOutgoing;
	private Module[] modulesIncoming;
	
	private List<UserViolation> violations;
	
	@Getter @Setter private boolean forceSave;
	
	private Set<Module> incomingPacketsBufferRequests;
	private Deque<IMinecraftIncomingPacket> incomingPacketsBuffer;
	
	protected PrintWriter violationPrintWriter;
	
	public UserEvaluation(BaseUser user)
	{
		this.user = user;
		
		this.violations = new ArrayList<>(0);
		
		this.incomingPacketsBufferRequests = new HashSet<>();
		this.incomingPacketsBuffer = new ArrayDeque<>(0);
	}
	
	public void setup() throws Exception
	{
		this.buildModules();
	}
	
	private void buildModules() throws Exception
	{
		List<Module> modulesList = this.generateModules(); //Used to sort modules to arrays
		
		//Optimization, only the needed ones, IN ARRAY
		this.modulesIncoming = modulesList.stream().filter((m) -> m.handlesOutgoingPackets()).toArray(Module[]::new);
		this.modulesOutgoing = modulesList.stream().filter((m) -> m.handlesIncomingPackets()).toArray(Module[]::new);
	}

	protected List<Module> generateModules() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		return this.generateModules(UserEvaluation.DEFAULT_MODULES);
	}
	
	protected List<Module> generateModules(Set<Class<? extends Module>> modules) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
	{
		List<Module> modulesList = new ArrayList<>();
		
		GameStatusAwareModule godModule = new GameStatusAwareModule(this);
		
		modulesList.add(godModule);
		
		for(Class<? extends Module> clazz : modules)
		{
			Module module;
			
			try
			{
				Constructor<? extends Module> constructor = clazz.getConstructor(GameStatusAwareModule.class, UserEvaluation.class);
				if (constructor != null)
				{
					module = constructor.newInstance(godModule, this);

					if (module.pre())
					{
						godModule.addSubmodule((GameStatusAwareSubmodule)module);
					}
					
					continue;
				}
			}
			catch(NoSuchMethodException ignored)
			{
			}

			try
			{
				Constructor<? extends Module> constructor = clazz.getConstructor(UserEvaluation.class);
				if (constructor != null)
				{
					module = constructor.newInstance(this);
					
					if (module.pre())
					{
						modulesList.add(module);
					}
				}
				else
				{
					throw new RuntimeException("Could not find constructor for " + clazz);
				}
			}
			catch(NoSuchMethodException ignored)
			{
				
			}
		}
		
		return modulesList;
	}

	public void requestIncomingBuffering(Module module)
	{
		this.incomingPacketsBufferRequests.add(module);
	}
	
	public void read(UserPackets packets) throws Exception
	{
		try
		{
			this.writeDetailedLogOutgoing((writer) ->
			{
				this.violationPrintWriter = writer;
				
				packets.readOutgoing((p) ->
				{
					this.writeToLog(writer, p);
					
					for(Module module : this.modulesOutgoing)
					{
						if (!module.handlesAsyncPackets() && p.isAsync())
						{
							continue;
						}
						
						module.analyzeOutgoing(p);
					}
				});
				
				//We have processed all outgoing packets, finish the analyze
				for(Module module : this.modulesOutgoing)
				{
					module.postAnalyzeOutgoing();
				}
			});
			
			this.writeDetailedLogIncoming((writer) ->
			{
				this.violationPrintWriter = writer;
				
				this.checkIncomingBuffering(writer);
				
				packets.readIncoming((p) -> this.tryReadIncoming(writer, p));
				
				//We have processed all incoming packets, finish the analyze
				for(Module module : this.modulesOutgoing)
				{
					module.postAnalyzeIncoming();
				}
			});
		}
		catch(Throwable e)
		{
			UserEvaluation.LOGGER.fatal("User evaluation has failed for user " + this.getUser(), e);
			
			this.onException(e);
		}
	}
	
	private void checkIncomingBuffering(PrintWriter writer)
	{
		if (!this.incomingPacketsBufferRequests.isEmpty())
		{
			Iterator<Module> modules = this.incomingPacketsBufferRequests.iterator();
			while (modules.hasNext())
			{
				Module module = modules.next();

				if (!module.requestsIncomingBuffering())
				{
					modules.remove();
				}
			}
			
			while(!this.incomingPacketsBuffer.isEmpty())
			{
				if (!this.incomingPacketsBufferRequests.isEmpty())
				{
					break;
				}

				this.readIncoming(writer, this.incomingPacketsBuffer.poll());
			}
		}
	}
	
	private void tryReadIncoming(PrintWriter writer, IMinecraftIncomingPacket packet)
	{
		if (!this.incomingPacketsBufferRequests.isEmpty())
		{
			this.incomingPacketsBuffer.add(packet);
			
			return;
		}
		
		this.readIncoming(writer, packet);
	}
	
	private void readIncoming(PrintWriter writer, IMinecraftIncomingPacket packet)
	{
		this.writeToLog(writer, packet);
		
		for(Module module : this.modulesIncoming)
		{
			if (!module.handlesAsyncPackets() && packet.isAsync())
			{
				continue;
			}
			
			if (!module.handlesPluginMessages() && packet instanceof IPluginMessageIncomingPacket)
			{
				continue;
			}
			
			module.analyzeIncoming(packet);
		}
	}
	
	protected void onException(Throwable e) throws IOException
	{
		this.user.onException(e);
	}
	
	public void writeDetailedLogIncoming(IOCallback<PrintWriter> run) throws IOException
	{
		run.write(null);
	}
	
	public void writeDetailedLogOutgoing(IOCallback<PrintWriter> run) throws IOException
	{
		run.write(null);
	}
	
	public void writeToLog(PrintWriter writer, Object object)
	{
		//NOP
	}

	public void writeToLog(PrintWriter writer, IMinecraftPacket object)
	{
		//NOP
	}
	
	public void writeToLog(IOCallback<PrintWriter> run)
	{
		//NOP
	}
	
	public void addViolation(UserViolation violation)
	{
		this.violations.add(violation);
	}

	public void close()
	{
		try
		{
			this.writeViolations();
		}
		catch (Throwable e)
		{
			UserEvaluation.LOGGER.error(e);
		}
	}
	
	private void writeViolations() throws IOException
	{
		this.getUser().createEntry("violations.txt", (writer) ->
		{
			for(UserViolation violation : this.violations)
			{
				writer.println(violation);
			}
		});
	}
	
	public List<UserViolation> getViolations()
	{
		return Collections.unmodifiableList(this.violations);
	}
}
