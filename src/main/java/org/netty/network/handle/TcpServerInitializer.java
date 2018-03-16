/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.handle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

@Component
public class TcpServerInitializer extends ChannelInitializer<SocketChannel> {

	@Autowired
	private TcpChannelHandler tcpChannelHandler;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		/*
		 * 添加消息解码器,设置每个数据包最大长度为1024,防止缓冲区溢出 数据包长度字段为开始位置，且占有4个字节
		 */
		pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4));
		// 添加消息编解码器
		pipeline.addLast(new CommandCodecHandler());
		// 添加业务处理类
		pipeline.addLast(tcpChannelHandler);
	}

}
