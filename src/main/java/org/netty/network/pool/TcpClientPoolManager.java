package org.netty.network.pool;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
	private static AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool> tcpClientPoolMap;

	private static TcpChannelPoolHandler tcpChannelPoolHandler;
	
	@Autowired
	private TcpChannelPoolHandler poolHandler;
	
	@PostConstruct
	public void init() {
		TcpClientPoolManager.tcpChannelPoolHandler = poolHandler;
	}
	
	static {
		initTcpClientPool();
	}
	
	private static void initTcpClientPool() {
		bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true);

		tcpClientPoolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
			@Override
			protected SimpleChannelPool newPool(InetSocketAddress key) {
				return new FixedChannelPool(bootstrap.remoteAddress(key), tcpChannelPoolHandler, maxConnections);
			}
		};
	}
	
	public SimpleChannelPool getTcpClientPool(String ip, int port) {
		return tcpClientPoolMap.get(new InetSocketAddress(ip, port));
	}
}
