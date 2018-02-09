/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.handle;

import java.net.SocketAddress;

import org.netty.network.annotation.AbstractAnnotationScanner;
import org.netty.network.codec.Command;
import org.netty.network.processor.CmdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
@Component
public class TcpChannelHandler extends SimpleChannelInboundHandler<Command<?>>{

	private static final Logger logger = LoggerFactory.getLogger(TcpChannelHandler.class);
	
	@Autowired
	private AbstractAnnotationScanner abstractAnnotationScanner;
	
	/**
	 * 处理tcp请求的异步的线程池
	 */
    @Autowired
    @Qualifier("asyncTcpThreadPool")
    private ThreadPoolTaskExecutor asyncTcpThreadPool;
    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketAddress socketAddress = ctx.channel().remoteAddress();
		logger.info("远程主机连接成功,远程主机:{}",socketAddress);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.info("Channel发生异常:", cause);
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Command<?> command) throws Exception {
		CmdProcessor<?> cmdProcessor = abstractAnnotationScanner.getProcessor(command.getCmd());
		if (cmdProcessor != null) {
			asyncTcpThreadPool.execute(new CmdProcessTask(cmdProcessor, ctx, command));
		} else {
			logger.warn("没有找到对应的CmdProcessor,command is :{}",command.getCmd());
		}
	}

	/**
     * 异步执行任务
     */
    private static class CmdProcessTask implements Runnable{
        private CmdProcessor<?> cmdProcessor;
        private ChannelHandlerContext ctx;
        private Command<?> command;

        public CmdProcessTask(CmdProcessor<?> cmdProcessor, ChannelHandlerContext ctx, Command<?> command) {
            this.cmdProcessor = cmdProcessor;
            this.ctx = ctx;
            this.command = command;
        }

        @Override
        public void run() {
            try {
                cmdProcessor.process(ctx,command);
            } catch (Exception e) {
            	logger.error("处理Cmd失败:",e);
            }
        }
    }
}
