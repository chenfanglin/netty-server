package org.netty.network.pool;

import org.netty.network.codec.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

@Component
public class TcpClientPool {

	private static final Logger logger = LoggerFactory.getLogger(TcpClientPool.class);
	
	@Autowired
	private TcpClientPoolManager tcpClientPoolManager;
	
	@Autowired
	private TcpClientChannelHandler tcpClientChannelHandler;
	
	/**
	 * 同步发送,有返回结果
	 * @param ip
	 * @param port
	 * @param msg
	 * @return
	 */
	public Command<?> syncSendMsg(String ip, int port, Command<?> msg) {
		SimpleChannelPool pool = tcpClientPoolManager.getTcpClientPool(ip, port);
		Future<Channel> future = pool.acquire();
		try {
			Channel channel = future.get();
			if (channel.isWritable()) {
				channel.writeAndFlush(msg);
			}
			pool.release(channel);
			return tcpClientChannelHandler.getResponseMsg(msg.getHeader().getClientSeq());
		} catch (Exception e) {
			logger.error("syncSendMsg异常:" + e);
		} 
		return null;
	}
	
	/**
	 * 异步发送，没有返回结果
	 * @param ip
	 * @param port
	 * @param msg
	 */
	public void asyncSendMsg(String ip, int port, Command<?> msg) {
		try {
			SimpleChannelPool pool = tcpClientPoolManager.getTcpClientPool(ip, port);
			Future<Channel> future = pool.acquire();
			/**
			 * java 8 新特性
			 */
			future.addListener((FutureListener<Channel>) f -> {
                if (f.isSuccess()) {
                    Channel ch = f.getNow();
                    ch.writeAndFlush(msg);
                    pool.release(ch);
                    tcpClientChannelHandler.getResponseMsg(msg.getHeader().getClientSeq());
                }
            });
		} catch (Exception e) {
			logger.error("asyncSendMsg异常:" + e);
		}
	}
}
