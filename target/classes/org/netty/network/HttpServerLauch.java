/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

@Component
public class HttpServerLauch {

	@Autowired
	@Qualifier("httpServerBootstrap")
	private ServerBootstrap serverBootstrap;

	@Value("${http.port}")
    private int httpPort;
	
	private ChannelFuture serverChannelFuture;
	
	@PostConstruct
    public void start() throws Exception {
        serverChannelFuture = serverBootstrap.bind(httpPort).sync();
    }

    @PreDestroy
    public void stop() throws Exception {
        serverChannelFuture.channel().closeFuture().sync();
    }

}
