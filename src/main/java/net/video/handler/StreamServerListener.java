package net.video.handler;

import org.jboss.netty.channel.Channel;

public interface StreamServerListener {
	public void onClientConnectedIn(Channel channel);
	public void onClientDisconnected(Channel channel);
	public void onException(Channel channel, Throwable t);
}
