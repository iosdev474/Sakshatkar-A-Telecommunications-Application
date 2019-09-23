package net.video.handler;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;

@Slf4j
public class StreamServerHandler extends SimpleChannelHandler{
	protected final StreamServerListener streamServerListener;

	

	public StreamServerHandler(StreamServerListener streamServerListener) {
		super();
		this.streamServerListener = streamServerListener;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		Throwable t = e.getCause();
		log.debug("exception caught at :{},exception :{}",channel,t);
		streamServerListener.onException(channel, t);
		//super.exceptionCaught(ctx, e);
	}
	
	

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		log.info("channel connected :{}",channel);
		streamServerListener.onClientConnectedIn(channel);
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		Channel channel = e.getChannel();
		log.info("channel disconnected :{}",channel);
		streamServerListener.onClientDisconnected(channel);
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		long size = e.getWrittenAmount();
		log.info("frame send at :{} size :{}",channel,size);
		super.writeComplete(ctx, e);
	}
	
	
	
	
}
