package org.netty.network.pool;

import java.net.InetSocketAddress;

import org.springframework.stereotype.Component;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component
public class TcpClientPoolManager {

	private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
	
	private static Bootstrap bootstrap = new Bootstrap();
	
	private static final int maxConnections = Runtime.getRuntime().availableProcessors() * 2;
	
	// key 是地址， value是pool，即一个地址一个pool
	private AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> tcpClientPoolMap;

	private void initTcpClientPool() {
		bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true);

		tcpClientPoolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
			@Override
			protected SimpleChannelPool newPool(InetSocketAddress key) {
				return new FixedChannelPool(bootstrap.remoteAddress(key), new TcpChannelPoolHandler(), maxConnections);
			}
		};
	}
	
	public SimpleChannelPool getTcpClientPool(String ip, int port) {
		TcpClientPoolManager manager = new TcpClientPoolManager();
		manager.initTcpClientPool();
		return tcpClientPoolMap.get(new InetSocketAddress(ip, port));
	}
	
}
