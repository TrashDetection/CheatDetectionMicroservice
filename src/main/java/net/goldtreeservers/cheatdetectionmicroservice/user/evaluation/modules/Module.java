package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.modules;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;

public abstract class Module
{
	@Getter private final UserEvaluation evaluation;
	
	private Map<Class<? extends IMinecraftIncomingPacket>, PacketHandlerEntry<? super IMinecraftIncomingPacket>> incomingPacketHandler;
	private Map<Class<? extends IMinecraftOutgoingPacket>, PacketHandlerEntry<? super IMinecraftOutgoingPacket>> outgoingPacketHandler;
	
	public Module(UserEvaluation evaluation)
	{
		this.evaluation = evaluation;
		
		this.incomingPacketHandler = new IdentityHashMap<>();
		this.outgoingPacketHandler = new IdentityHashMap<>();
	}
	
	public boolean pre()
	{
		return true;
	}
	
	public boolean handlesAsyncPackets()
	{
		return false;
	}
	
	public boolean handlesPluginMessages()
	{
		return false;
	}
	
	public boolean requestsIncomingBuffering()
	{
		return false;
	}
	
	public abstract boolean handlesOutgoingPackets();
	public abstract boolean handlesIncomingPackets();
	
	public void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		this.fireIncomingPacketHandler(packet);
	}
	
	public void postAnalyzeIncoming()
	{
	}
	
	public void analyzeOutgoing(IMinecraftOutgoingPacket packet)
	{
		this.fireOutgoingPacketHandler(packet);
	}
	
	public void postAnalyzeOutgoing()
	{
		
	}
	
	//Helper
	protected final PacketHandlerEntry<? super IMinecraftIncomingPacket> getIncomingPacketHandlerEntry(IMinecraftIncomingPacket packet)
	{
		return this.incomingPacketHandler.get(packet.getBaseType());
	}

	//Helper
	protected final PacketHandlerEntry<? super IMinecraftOutgoingPacket> getOutgoingPacketHandlerEntry(IMinecraftOutgoingPacket packet)
	{
		return this.outgoingPacketHandler.get(packet.getBaseType());
	}

	//Helper
	protected final boolean fireIncomingPacketHandler(IMinecraftIncomingPacket packet)
	{
		PacketHandlerEntry<? super IMinecraftIncomingPacket> handler = this.getIncomingPacketHandlerEntry(packet.getBaseType());
		if (handler != null)
		{
			handler.firePre(packet);
			handler.firePost(packet);
			
			return true;
		}
		
		return false;
	}
	
	//Helper
	protected final boolean fireOutgoingPacketHandler(IMinecraftOutgoingPacket packet)
	{
		PacketHandlerEntry<? super IMinecraftOutgoingPacket> handler = this.getOutgoingPacketHandlerEntry(packet.getBaseType());
		if (handler != null)
		{
			handler.firePre(packet);
			handler.firePost(packet);
			
			return true;
		}
		
		return false;
	}

	//Helper
	@SuppressWarnings("unchecked")
	private <T extends IMinecraftIncomingPacket> PacketHandlerEntry<T> getIncomingPacketHandlerEntry(Class<? extends IMinecraftIncomingPacket> clazz)
	{
		return (PacketHandlerEntry<T>) this.incomingPacketHandler.computeIfAbsent(clazz, (key) ->
		{
			return new PacketHandlerEntry<>();
		});
	}

	//Helper
	@SuppressWarnings("unchecked")
	private <T extends IMinecraftOutgoingPacket> PacketHandlerEntry<T> getOutgoingPacketHandlerEntry(Class<? extends IMinecraftOutgoingPacket> clazz)
	{
		return (PacketHandlerEntry<T>) this.outgoingPacketHandler.computeIfAbsent(clazz, (key) ->
		{
			return new PacketHandlerEntry<>();
		});
	}
	
	//Helper
	protected final <T extends IMinecraftIncomingPacket> void addIncomingHandlerPre(Class<? extends IMinecraftIncomingPacket> clazz, Consumer<T> handler)
	{
		PacketHandlerEntry<T> entry = this.getIncomingPacketHandlerEntry(clazz);
		entry.pre = handler;
	}

	//Helper
	protected final <T extends IMinecraftIncomingPacket> void addIncomingHandlerPost(Class<? extends IMinecraftIncomingPacket> clazz, Consumer<T> handler)
	{
		PacketHandlerEntry<T> entry = this.getIncomingPacketHandlerEntry(clazz);
		entry.post = handler;
	}

	//Helper
	protected final <T extends IMinecraftOutgoingPacket> void addOutgoingHandlerPre(Class<? extends IMinecraftOutgoingPacket> clazz, Consumer<T> handler)
	{
		PacketHandlerEntry<T> entry = this.getOutgoingPacketHandlerEntry(clazz);
		entry.pre = handler;
	}

	//Helper
	protected final <T extends IMinecraftOutgoingPacket> void addOutgoingHandlerPost(Class<? extends IMinecraftOutgoingPacket> clazz, Consumer<T> handler)
	{
		PacketHandlerEntry<T> entry = this.getOutgoingPacketHandlerEntry(clazz);
		entry.post = handler;
	}
	
	//Helper
	protected final void addViolation(UserViolation violation)
	{
		this.evaluation.addViolation(violation);
	}

	//Helper
	protected final void requestIncomingBuffering()
	{
		this.evaluation.requestIncomingBuffering(this);
	}
	
	protected static class PacketHandlerEntry<T>
	{
		private Consumer<T> pre;
		private Consumer<T> post;
		
		protected void firePre(T packet)
		{
			if (this.pre != null)
			{
				this.pre.accept(packet);
			}
		}
		
		protected void firePost(T packet)
		{
			if (this.post != null)
			{
				this.post.accept(packet);
			}
		}
	}
}
