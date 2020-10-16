package net.goldtreeservers.cheatdetectionmicroservice.minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.IMinecraftHook;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftOutgoingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IPacketBuffer;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.shared.SharedMinecraftHooks;
import lombok.Getter;
import net.goldtreeservers.cheatdetectionmicroservice.utils.ByteBufUtils;

public class MinecraftPacketManager
{
	private static final Map<Integer, MinecraftPacketManager> hooks = new ConcurrentHashMap<>();
	
	@Getter private final IMinecraftHook hook;
	
	public MinecraftPacketManager(IMinecraftHook hook)
	{
		this.hook = hook;
	}
	
	public IMinecraftIncomingPacket readIncomingPacket(IPacketBuffer buffer)
	{
		int packetId = ByteBufUtils.readVarInt(buffer.getBuffer());
		
		try
		{
			IMinecraftPacket packet = this.hook.getNetworkHook().getIncomingPlayPackets().get(packetId).newInstance();
			packet.readPacket(buffer);

			if (packet instanceof IMinecraftIncomingPacket)
			{
				return (IMinecraftIncomingPacket)packet;
			}
			
			return UnparsedIncomingPacket.newInstance(packetId);
		}
		catch(Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public IMinecraftOutgoingPacket readOutgoingPacket(IPacketBuffer buffer)
	{
		int packetId = ByteBufUtils.readVarInt(buffer.getBuffer());

		try
		{
			IMinecraftPacket packet = this.hook.getNetworkHook().getOutgoingPlayPackets().get(packetId).newInstance();
			
			try
			{
				packet.readPacket(buffer);
			}
			catch(Throwable e)
			{
				throw new RuntimeException("There was a problem trying to read packet " + packet.getClass(), e);
			}
			
			if (packet instanceof IMinecraftOutgoingPacket)
			{
				return (IMinecraftOutgoingPacket)packet;
			}
			
			return UnparsedOutgoingPacket.newInstance(packetId);
		}
		catch(Throwable e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static MinecraftPacketManager getPacketManager(int protocolVersion, boolean inspect)
	{
		return MinecraftPacketManager.hooks.computeIfAbsent(protocolVersion, (k) ->
		{
			IMinecraftHook hook = SharedMinecraftHooks.getHook(k);
			if (hook == null)
			{
				return null;
			}
			
			return new MinecraftPacketManager(hook);
		});
	}
	
	private static abstract class UnparsedPacket implements IMinecraftPacket
	{
		private final int packetId;
		
		private String cachedToString;
		
		private UnparsedPacket(int packetId)
		{
			this.packetId = packetId;
		}
		
		@Override
		public String asString()
		{
			if (this.cachedToString == null)
			{
				this.cachedToString = "Unknown Packet: 0x" + Integer.toHexString(this.packetId);
			}
			
			return this.cachedToString;
		}
	}
	
	private static class UnparsedIncomingPacket extends UnparsedPacket implements IMinecraftIncomingPacket
	{
		private static final Map<Integer, UnparsedIncomingPacket> PACKETS = new ConcurrentHashMap<>();

		public UnparsedIncomingPacket(int protocolVersion)
		{
			super(protocolVersion);
		}

		@Override
		public Class<? extends IMinecraftIncomingPacket> getBaseType()
		{
			return UnparsedIncomingPacket.class;
		}

		public static UnparsedIncomingPacket newInstance(int packetId)
		{
			return UnparsedIncomingPacket.PACKETS.computeIfAbsent(packetId, (key) ->
			{
				return new UnparsedIncomingPacket(key);
			});
		}
	}
	
	private static class UnparsedOutgoingPacket extends UnparsedPacket implements IMinecraftOutgoingPacket
	{
		private static final Map<Integer, UnparsedOutgoingPacket> PACKETS = new ConcurrentHashMap<>();

		public UnparsedOutgoingPacket(int protocolVersion)
		{
			super(protocolVersion);
		}

		@Override
		public Class<? extends IMinecraftOutgoingPacket> getBaseType()
		{
			return UnparsedOutgoingPacket.class;
		}

		public static UnparsedOutgoingPacket newInstance(int packetId)
		{
			return UnparsedOutgoingPacket.PACKETS.computeIfAbsent(packetId, (key) ->
			{
				return new UnparsedOutgoingPacket(key);
			});
		}
	}
}
