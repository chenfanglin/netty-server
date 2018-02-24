package org.netty.network.pool;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.netty.network.codec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Component
@ChannelHandler.Sharable
public class TcpClientChannelHandler extends SimpleChannelInboundHandler<Command<?>> {

	private static final Logger logger = LoggerFactory.getLogger(TcpClientChannelHandler.class);
	
	private Map<Long, BlockingQueue<Command<?>>> responseMap = new ConcurrentHashMap<Long, BlockingQueue<Command<?>>>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Command<?> msg) throws Exception {
		long cilentSeq = msg.getHeader().getClientSeq();
		BlockingQueue<Command<?>> queue = responseMap.get(cilentSeq);
		if (queue == null) {
			queue = new LinkedBlockingQueue<Command<?>>(1);
		} 
		queue.add(msg);
		responseMap.put(cilentSeq, queue);
	}

	public Command<?> getResponseMsg(final long cilentSeq) {
		responseMap.putIfAbsent(cilentSeq, new LinkedBlockingQueue<Command<?>>(1));
		BlockingQueue<Command<?>> queue = responseMap.get(cilentSeq);
		try {
			Command<?> command = queue.take();
			return command;
		} catch (Exception e) {
			logger.error("获取服务返回结果异常:" + e);
		} finally {
			responseMap.remove(cilentSeq);
		}
		return null;
	}
}
