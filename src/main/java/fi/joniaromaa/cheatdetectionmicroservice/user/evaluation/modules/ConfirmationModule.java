package fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.modules;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.InspectUserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.violations.inspect.InspectInformationViolation;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.IMinecraftOutgoingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IClickWindowIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.incoming.IConfirmTransactionIncomingPacket;
import fi.joniaromaa.minecrafthook.common.network.outgoing.IConfirmTransactionOutgoingPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import lombok.Getter;

public abstract class ConfirmationModule extends Module
{
	private static final byte PACKET_DONT_CONFIRM = 0;
	private static final byte PACKET_CONFIRM = 1;
	private static final byte PACKET_CONFIRM_SPECIAL = 2;

	@Getter private int currentConfirmationIdOutgoing;
	@Getter private int currentConfirmationIdIncoming;
	
	private boolean onConfirmationStateIncoming;
	private boolean onConfirmationStateOutgoing;
	
	private IMinecraftOutgoingPacket lastPacketOutgoing;
	private Deque<IMinecraftIncomingPacket> lastPacketsIncoming;

	private Int2ObjectMap<IMinecraftOutgoingPacket> confirmedPackets;
	
	private Object2ByteMap<Class<? extends IMinecraftOutgoingPacket>> packetsToConfirm;
	private Map<Class<? extends IMinecraftOutgoingPacket>, Consumer<? super IMinecraftOutgoingPacket>> packetConfirmationHandlers;
	
	public ConfirmationModule(UserEvaluation evaluation)
	{
		super(evaluation);
		
		this.lastPacketsIncoming = new ArrayDeque<>(2);
		
		this.confirmedPackets = new Int2ObjectOpenHashMap<>();
		
		this.packetsToConfirm = new Object2ByteOpenHashMap<>();
		this.packetConfirmationHandlers = new IdentityHashMap<>();
		
		this.addOutgoingHandlerPre(IConfirmTransactionOutgoingPacket.class, this::handlePacketConfirmationOutgoing);
		
		this.addIncomingHandlerPre(IConfirmTransactionIncomingPacket.class, this::handlePacketConfirmationIncoming);
	}
	
	@Override
	public boolean handlesOutgoingPackets()
	{
		return true;
	}

	@Override
	public boolean handlesIncomingPackets()
	{
		return true;
	}
	
	@Override
	public boolean requestsIncomingBuffering()
	{
		return this.getCurrentConfirmationIdOutgoing() == this.getCurrentConfirmationIdIncoming() && this.onConfirmationStateIncoming;
	}
	
	private boolean shouldConfirm(IMinecraftOutgoingPacket packet)
	{
		if (packet == null)
		{
			return false;
		}

		byte value = this.packetsToConfirm.getByte(packet.getBaseType());
		if (value == ConfirmationModule.PACKET_DONT_CONFIRM)
		{
			return false;
		}
		else if (value == ConfirmationModule.PACKET_CONFIRM_SPECIAL)
		{
			return this.shouldConfirmSpecial(packet);
		}
		
		return true;
	}
	
	protected abstract boolean shouldConfirmSpecial(IMinecraftOutgoingPacket packet);
	
	@Override
	public final void analyzeOutgoing(IMinecraftOutgoingPacket packet)
	{
		if (packet instanceof IConfirmTransactionOutgoingPacket)
		{
			if (this.handlePacketConfirmationOutgoing((IConfirmTransactionOutgoingPacket)packet))
			{
				return;
			}
		}
		
		this.checkLastPacketConfirmationStatus();
		
		this.lastPacketOutgoing = packet;
		
		this.fireOutgoing(packet);
	}
	
	private void fireOutgoing(IMinecraftOutgoingPacket packet)
	{
		PacketHandlerEntry<? super IMinecraftOutgoingPacket> entry = super.getOutgoingPacketHandlerEntry(packet);
		if (entry == null)
		{
			this.analyzeOutgoing0(packet);
			
			return;
		}
		
		entry.firePre(packet);

		this.analyzeOutgoing0(packet);
		
		entry.firePost(packet);
	}
	
	private boolean handlePacketConfirmationOutgoing(IConfirmTransactionOutgoingPacket packet)
	{
		if (packet.getWindowId() == IClickWindowIncomingPacket.PLAYER_INVENTORY_ID)
		{
			if (packet.getActionNumber() == IConfirmTransactionIncomingPacket.CONFIRM_TRANSACTION_ID)
			{
				this.onConfirmationStateOutgoing = false;
				
				//Even add null packets! Keep the count correct!
				this.confirmedPackets.put(this.currentConfirmationIdOutgoing++, this.lastPacketOutgoing);
				
				//We confirmed this packet, now clear it so we don't check it next time
				this.lastPacketOutgoing = null;
				
				this.receivedOutgoingConfirmationPacket();

				if (!InspectUserEvaluation.SUPRESS_PACKET_CONFIRMATIONS)
				{
					this.getEvaluation().writeToLog((writer) ->
					{
						writer.println(new InspectInformationViolation("Confirmation Id: " + this.getCurrentConfirmationIdOutgoing()));
					});
				}
				
				return true;
			}
			else if (packet.getActionNumber() == IConfirmTransactionIncomingPacket.CONFIRM_TRANSACTION_PREV_ID)
			{
				this.checkLastPacketConfirmationStatus();
				
				this.onConfirmationStateOutgoing = true;
				
				//Don't add the magic packet as our last one, also make sure its cleared out
				this.lastPacketOutgoing = null;

				this.receivedOutgoingPreConfirmationPacket();
				
				return true;
			}
		}
		
		return false;
	}
	
	private void checkLastPacketConfirmationStatus()
	{
		if (this.shouldConfirm(this.lastPacketOutgoing))
		{
			throw new RuntimeException(String.format("Confirmed packet was missed... y? | Module: %s | Id: %d | Packet: %s", this.getClass(), this.currentConfirmationIdOutgoing, this.lastPacketOutgoing));
		}
	}
	
	protected void receivedOutgoingConfirmationPacket()
	{
		//NOP
	}
	
	protected void receivedOutgoingPreConfirmationPacket()
	{
		//NOP
	}
	
	protected void analyzeOutgoing0(IMinecraftOutgoingPacket packet)
	{
		//NOP
	}
	
	@Override
	public void postAnalyzeOutgoing()
	{
		if (!this.onConfirmationStateOutgoing)
		{
			this.checkLastPacketConfirmationStatus();
		}
	}
	
	@Override
	public final void analyzeIncoming(IMinecraftIncomingPacket packet)
	{
		this.analyzeIncomingInternal(packet);
		
		if (this.requestsIncomingBuffering())
		{
			this.requestIncomingBuffering();
		}
	}
	
	private void analyzeIncomingInternal(IMinecraftIncomingPacket packet)
	{
		if (packet instanceof IConfirmTransactionIncomingPacket)
		{
			if (this.handlePacketConfirmationIncoming((IConfirmTransactionIncomingPacket)packet))
			{
				return;
			}
		}
		
		//If we are in the middle of confirmation, queue up all the packets and wait for confirmation packet to bulk these after the packet confirmation
		if (this.onConfirmationStateIncoming)
		{
			this.lastPacketsIncoming.add(packet);

			return;
		}

		this.fireIncoming(packet);
	}
	
	private void fireIncoming(IMinecraftIncomingPacket packet)
	{
		PacketHandlerEntry<? super IMinecraftIncomingPacket> entry = super.getIncomingPacketHandlerEntry(packet);
		if (entry == null)
		{
			this.analyzeIncoming0(packet);
			
			return;
		}
		
		entry.firePre(packet);

		this.analyzeIncoming0(packet);
		
		entry.firePost(packet);
	}
	
	private boolean handlePacketConfirmationIncoming(IConfirmTransactionIncomingPacket packet)
	{
		if (packet.getWindowId() == IClickWindowIncomingPacket.PLAYER_INVENTORY_ID)
		{
			if (packet.getActionNumber() == IConfirmTransactionIncomingPacket.CONFIRM_TRANSACTION_ID)
			{
				this.onConfirmationStateIncoming = false;

				IMinecraftOutgoingPacket confirmed = this.confirmedPackets.remove(this.currentConfirmationIdIncoming++);
				if (this.shouldConfirm(confirmed))
				{
					this.packetConfirmed(confirmed);
				}
				
				while (!this.lastPacketsIncoming.isEmpty())
				{
					this.fireAnalyzeConfirmation(confirmed, this.lastPacketsIncoming.pop());
				}

				this.receivedIncomingConfirmationPacket();
				
				if (!InspectUserEvaluation.SUPRESS_PACKET_CONFIRMATIONS)
				{
					this.getEvaluation().writeToLog((writer) ->
					{
						writer.println(new InspectInformationViolation("Confirmation Id: " + this.getCurrentConfirmationIdIncoming()));
					});
				}

				return true;
			}
			else if (packet.getActionNumber() == IConfirmTransactionIncomingPacket.CONFIRM_TRANSACTION_PREV_ID)
			{
				this.onConfirmationStateIncoming = true;

				this.receivedIncomingPreConfirmationPacket();

				return true;
			}
		}
		
		return false;
	}
	
	private void fireAnalyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		PacketHandlerEntry<? super IMinecraftIncomingPacket> entry = super.getIncomingPacketHandlerEntry(packet);
		if (entry == null)
		{
			this.analyzeIncoming0(packet);
			
			return;
		}
		
		entry.firePre(packet);
		
		this.analyzeConfirmation(confirmed, packet);
		
		entry.firePost(packet);
	}
	
	protected void packetConfirmed(IMinecraftOutgoingPacket packet)
	{
		Consumer<? super IMinecraftOutgoingPacket> handler = this.packetConfirmationHandlers.get(packet.getBaseType());
		if (handler != null)
		{
			handler.accept(packet);
		}
	}
	
	protected void receivedIncomingConfirmationPacket()
	{
		//NOP
	}
	
	protected void receivedIncomingPreConfirmationPacket()
	{
		//NOP
	}
	
	protected void analyzeIncoming0(IMinecraftIncomingPacket packet)
	{
		//NOP
	}

	protected void analyzeConfirmation(IMinecraftOutgoingPacket confirmed, IMinecraftIncomingPacket packet)
	{
		this.analyzeIncoming0(packet);
	}

	//Helper
	protected final IMinecraftOutgoingPacket getPacket(int confirmationId)
	{
		return this.confirmedPackets.get(confirmationId);
	}

	//Helper
	protected final void addConfirmPacket(Class<? extends IMinecraftOutgoingPacket> clazz)
	{
		this.addConfirmPacket(clazz, false);
	}

	//Helper
	protected final void addConfirmPacket(Class<? extends IMinecraftOutgoingPacket> clazz, boolean special)
	{
		if (special)
		{
			this.packetsToConfirm.put(clazz, ConfirmationModule.PACKET_CONFIRM_SPECIAL);
		}
		else
		{
			this.packetsToConfirm.putIfAbsent(clazz, ConfirmationModule.PACKET_CONFIRM);
		}
	}

	//Helper
	protected final <T extends IMinecraftOutgoingPacket> void addConfirmPacket(Class<? extends IMinecraftOutgoingPacket> clazz, Consumer<T> handler)
	{
		this.addConfirmPacket(clazz, handler, false);
	}
	
	//Helper
	@SuppressWarnings("unchecked")
	protected final <T extends IMinecraftOutgoingPacket> void addConfirmPacket(Class<? extends IMinecraftOutgoingPacket> clazz, Consumer<T> handler, boolean special)
	{
		this.packetsToConfirm.put(clazz, special ? ConfirmationModule.PACKET_CONFIRM_SPECIAL : ConfirmationModule.PACKET_CONFIRM);
		this.packetConfirmationHandlers.put(clazz, (Consumer<? super IMinecraftOutgoingPacket>) handler);
	}
}
