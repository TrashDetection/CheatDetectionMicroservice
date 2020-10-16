package net.goldtreeservers.cheatdetectionmicroservice.user.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.IMinecraftPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.incoming.IConfirmTransactionIncomingPacket;
import fi.joniaromaacheatdetectionmicroserviceminecrafthook.network.outgoing.IConfirmTransactionOutgoingPacket;
import net.goldtreeservers.cheatdetectionmicroservice.core.CheatDetectionMicroservice;
import net.goldtreeservers.cheatdetectionmicroservice.user.BaseUser;
import net.goldtreeservers.cheatdetectionmicroservice.user.BaseUser.IOCallback;
import net.goldtreeservers.cheatdetectionmicroservice.user.evaluation.violations.UserViolation;

public class InspectUserEvaluation extends UserEvaluation
{
	public static final boolean SUPRESS_PACKET_CONFIRMATIONS = InspectUserEvaluation.getSupressPacketConfirmations();
	
	private ByteArrayOutputStream incoming;
	private ByteArrayOutputStream outgoing;
	
	private PrintWriter detailsIncoming;
	private PrintWriter detailsOutgoing;
	
	public InspectUserEvaluation(BaseUser user)
	{
		super(user);
	}
	
	@Override
	public void setup() throws Exception
	{
		super.setup();
		
		this.incoming = new ByteArrayOutputStream();
		this.outgoing = new ByteArrayOutputStream();
		
		this.detailsIncoming = new PrintWriter(this.incoming);
		this.detailsOutgoing = new PrintWriter(this.outgoing);
	}

	@Override
	public void writeDetailedLogIncoming(IOCallback<PrintWriter> run) throws IOException
	{
		run.write(this.detailsIncoming);
	}

	@Override
	public void writeDetailedLogOutgoing(IOCallback<PrintWriter> run) throws IOException
	{
		run.write(this.detailsOutgoing);
	}
	
	@Override
	public void writeToLog(PrintWriter writer, Object object)
	{
		writer.println(object);
	}

	@Override
	public void writeToLog(PrintWriter writer, IMinecraftPacket object)
	{
		if (InspectUserEvaluation.SUPRESS_PACKET_CONFIRMATIONS)
		{
			if (object instanceof IConfirmTransactionIncomingPacket || object instanceof IConfirmTransactionOutgoingPacket)
			{
				return;
			}
		}
		
		writer.println(object.asString());
	}
	
	@Override
	public void writeToLog(IOCallback<PrintWriter> run)
	{
		try
		{
			run.write(this.violationPrintWriter);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addViolation(UserViolation violation)
	{
		super.addViolation(violation);
		
		this.writeToLog(this.violationPrintWriter, violation);
	}
	
	@Override
	public void close()
	{
		super.close();

		this.detailsIncoming.flush();
		this.detailsOutgoing.flush();

		try
		{
			this.getUser().createEntryRaw("details-incoming.txt", (entry, writer) ->
			{
				writer.write(this.incoming.toByteArray());
			});
			
			this.getUser().createEntryRaw("details-outgoing.txt", (entry, writer) ->
			{
				writer.write(this.outgoing.toByteArray());
			});
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static boolean getSupressPacketConfirmations()
	{
		if (CheatDetectionMicroservice.DEBUG)
		{
			return System.getProperty("td.inspect.supress-packet-confirmations", "false").equalsIgnoreCase("true");
		}
		
		return true;
	}
}
