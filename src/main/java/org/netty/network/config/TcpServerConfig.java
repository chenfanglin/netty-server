/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.netty.network.handle.TcpServerInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

@Component
@EnableAsync
public class TcpServerConfig {

	@Value("${so.keepalive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

    @Autowired
    private TcpServerInitializer tcpServerInitializer;
    
    @Bean(name = "tcpBossGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup tcpBossGroup() {
        return new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    }

    @Bean(name = "tcpWorkerGroup", destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup tcpWorkerGroup() {
        return new NioEventLoopGroup();
    }
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    @Bean(name = "tcpServerBootstrap")
    public ServerBootstrap tcpServerBootstrap() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(tcpBossGroup(), tcpWorkerGroup()).channel(NioServerSocketChannel.class).handler(loggingHandler)
                .childHandler(tcpServerInitializer);
        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (ChannelOption option : keySet) {
            b.option(option, tcpChannelOptions.get(option));
        }
        return b;
    }

    /**
     * 处理tcp请求的异步的线程池
     * @return
     */
    @Bean(name = "asyncTcpThreadPool")
    public ThreadPoolTaskExecutor getAsyncTcpThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 8);
        threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 8);
        return threadPoolTaskExecutor;
    }

    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
        options.put(ChannelOption.SO_KEEPALIVE, keepAlive);
        options.put(ChannelOption.SO_BACKLOG, backlog);
        // 设置为非延迟发送，为true则不组装成大包发送，收到东西马上发出
        options.put(ChannelOption.TCP_NODELAY, true);
        // 立即关闭，不等待未送出的数据包
        options.put(ChannelOption.SO_LINGER, -1);
        // 设置每一个非主监听连接的端口可以重用
        options.put(ChannelOption.SO_REUSEADDR, true);
        options.put(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        return options;
    }
}
