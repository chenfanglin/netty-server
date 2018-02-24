package org.netty.network.pool;

import org.netty.network.handle.CommandCodecHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

@Component
public class TcpChannelPoolHandler implements ChannelPoolHandler {

	private static final Logger logger = LoggerFactory.getLogger(TcpChannelPoolHandler.class);

	@Value("${client.tcp.timeout}")
	private Integer timeout;

	private Integer lengthFiledLength = 4;

	@Autowired
	private TcpClientChannelHandler tcpClientChannelHandler;

	@Override
	public void channelReleased(Channel ch) throws Exception {
		logger.info("channelReleased: " + ch);
	}

	@Override
	public void channelAcquired(Channel ch) throws Exception {
		logger.info("channelAcquired: " + ch);
	}

	@Override
	public void channelCreated(Channel ch) throws Exception {
		logger.info("channelCreated: " + ch);
		SocketChannel socketChannel = (SocketChannel) ch;
		socketChannel.config().setKeepAlive(true);
		socketChannel.config().setTcpNoDelay(true);
		socketChannel.config().setConnectTimeoutMillis(timeout);
		socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, lengthFiledLength))
				.addLast(new CommandCodecHandler()).addLast(new ReadTimeoutHandler(timeout))
				.addLast(tcpClientChannelHandler);
	}

}
