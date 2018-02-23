/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;

@Component
public class HttpServerLauch {

	public static final Logger logger = LoggerFactory.getLogger(HttpServerLauch.class);
	
	@Autowired
	@Qualifier("httpServerBootstrap")
	private ServerBootstrap serverBootstrap;

	@Value("${http.port}")
    private int httpPort;
	
	private ChannelFuture serverChannelFuture;
	
	@PostConstruct
    public void start() {
		try {
			serverChannelFuture = serverBootstrap.bind(httpPort).sync();
		} catch (Exception e) {
			logger.error("HttpServerLauch start:" + e);
		}
    }

    @PreDestroy
    public void stop() {
    	try {
			serverChannelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("HttpServerLauch stop:" + e);
		}
    }

}
