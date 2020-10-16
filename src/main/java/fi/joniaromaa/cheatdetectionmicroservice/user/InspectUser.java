package fi.joniaromaa.cheatdetectionmicroservice.user;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.Deflater;

import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.InspectUserEvaluation;
import fi.joniaromaa.cheatdetectionmicroservice.user.evaluation.UserEvaluation;
import lombok.Getter;

public class InspectUser extends BaseUser
{
	@Getter private final int sessionId;
	
	public InspectUser(int sessionId, int userId, int protocolVersion, long timeAnalyzed, boolean inspect)
	{
		super(userId, protocolVersion);
		
		this.sessionId = sessionId;
		
		this.timeAnalyzed = timeAnalyzed;
		
		if (inspect)
		{
			this.evaluation = new InspectUserEvaluation(this);
		}
		else
		{
			this.evaluation = new UserEvaluation(this);
		}
	}
	
	@Override
	protected void createLogFile() throws IOException
	{
		super.createLogFile();
		
		this.outputFile.setLevel(Deflater.NO_COMPRESSION); //No compression when in inspect, save CPU
	}
	
	@Override
	public void analyze() throws Exception
	{
		super.analyze();
	}
	
	public void versionBump()
	{
		this.version++;
	}

	@Override
	public void writeUserInfoExtra(PrintWriter writer)
	{
		writer.println("RE-EVALUATION: TRUE");
	}
	
	@Override
	public File getLogFileFolder()
	{
		return new File("re-evaluation");
	}

	@Override
	public File getLogFile()
	{
		return new File(this.getUserId() + "_" + System.currentTimeMillis() + "_" + System.nanoTime() + "_re-evaluation.zip");
	}
}
