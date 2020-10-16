package net.goldtreeservers.cheatdetectionmicroservice.mocked.user;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import io.netty.buffer.ByteBuf;
import net.goldtreeservers.cheatdetectionmicroservice.mocked.utils.MockedUserPackets;
import net.goldtreeservers.cheatdetectionmicroservice.user.BaseUser;

public class MockedUser extends BaseUser
{
	public MockedUser(int protocolVersion)
	{
		super(-1, protocolVersion);
	}

	protected void setupPackets()
	{
		this.packets = new MockedUserPackets();
	}

	@Override
	protected void createLogFile()
	{
		//NOP
	}

	@Override
	public void close()
	{
		//NOP
	}

	@Override
	public void createEntry(String first, IOCallback<PrintWriter> callback) throws IOException
	{
		
	}

	@Override
	public void createEntryRaw(String first, ZipIOCallback<OutputStream> callback) throws IOException
	{
		//NOP
	}

	@Override
	public void createEntryRaw(String first, ZipIOCallback<OutputStream> callback, String... others) throws IOException
	{
		//NOP
	}

	@Override
	public void addIncomingBytes(long hash, ByteBuf buf)
	{
		//NOP
	}
	
	@Override
	public void addOutgoingBytes(long hash, ByteBuf buf)
	{
		//NOP
	}

	@Override
	public void onException(Throwable e) 
	{
		throw new RuntimeException(e);
	}

	@Override
	public void writeUserInfoExtra(PrintWriter writer)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public File getLogFileFolder()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public File getLogFile()
	{
		throw new UnsupportedOperationException();
	}
	
	public MockedUserPackets getPackets()
	{
		return (MockedUserPackets)this.packets;
	}
}
