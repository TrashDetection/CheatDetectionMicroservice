package net.goldtreeservers.cheatdetectionmicroservice.net.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;

public class ExceptionHandler extends ChannelDuplexHandler
{
	private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		ctx.close();
		
		if (cause instanceof TimeoutException)
		{
			ExceptionHandler.LOGGER.info("Connection from " + ctx.channel().remoteAddress() + " timed out");
			
			return;
		}
		
		ExceptionHandler.LOGGER.fatal("Connection from " + ctx.channel().remoteAddress() + " was faulted due to exception", cause);
    }
}
