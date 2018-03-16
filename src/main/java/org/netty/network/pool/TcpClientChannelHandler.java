package org.netty.network.pool;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.netty.network.codec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
@Component
public class TcpClientChannelHandler extends SimpleChannelInboundHandler<Command<?>> {

	private static final Logger logger = LoggerFactory.getLogger(TcpClientChannelHandler.class);
	
	/**
	 * TODO
	 * 这里需要使用一个有过期时间的map，防止内存溢出
	 */
	private Map<Long, BlockingQueue<Command<?>>> responseMap = new ConcurrentHashMap<Long, BlockingQueue<Command<?>>>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Command<?> msg) throws Exception {
		long cilentSeq = msg.getHeader().getClientSeq();
		BlockingQueue<Command<?>> queue = responseMap.get(cilentSeq);
		if (queue == null) {
			queue = new LinkedBlockingQueue<Command<?>>(1);
		} 
		// 这里为什么不使用add，因为add不成功的时候会抛出异常
		queue.offer(msg);
		responseMap.put(cilentSeq, queue);
	}

	public Command<?> getResponseMsg(final long cilentSeq) {
		responseMap.putIfAbsent(cilentSeq, new LinkedBlockingQueue<Command<?>>(1));
		BlockingQueue<Command<?>> queue = responseMap.get(cilentSeq);
		try {
			/**
			 * 为什么不使用take，因为take会一直阻塞，客户端一直得不到响应不是我们想要的
			 * 等待3秒
			 */
			Command<?> command = queue.poll(3000, TimeUnit.MILLISECONDS);
			return command;
		} catch (Exception e) {
			logger.error("获取服务返回结果异常:" + e);
		} finally {
			responseMap.remove(cilentSeq);
		}
		return null;
	}
}
