/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.handle;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

import java.lang.reflect.Method;
import java.net.SocketAddress;

import org.netty.network.annotation.AbstractAnnotationScanner;
import org.netty.network.annotation.RequestMapperHolder;
import org.netty.network.http.XXHttpRequest;
import org.netty.network.http.XXHttpResponse;
import org.netty.network.processor.BaseHttpService;
import org.netty.network.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

@ChannelHandler.Sharable
@Component
public class HttpChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

	@Autowired
	private AbstractAnnotationScanner abstractAnnotationScanner;

	/**
	 * 处理http请求的异步的线程池
	 */
    @Autowired
    @Qualifier("asyncHttpThreadPool")
    private ThreadPoolTaskExecutor asyncHttpThreadPool;
    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SocketAddress socketAddress = ctx.channel().remoteAddress();
		logger.info("远程主机连接成功,远程主机:{}", socketAddress);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Channel发生异常:", cause);
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
		try {
			if ("/favicon.ico".equals(fullHttpRequest.getUri())) {
	            return;
	        }
			XXHttpRequest request = null;
			if (fullHttpRequest.getMethod().equals(HttpMethod.GET)) {
				request = new XXHttpRequest(fullHttpRequest.getProtocolVersion(), HttpMethod.GET,
						fullHttpRequest.getUri());
			}
			if (fullHttpRequest.getMethod().equals(HttpMethod.POST)) {
				request = new XXHttpRequest(fullHttpRequest.getProtocolVersion(), HttpMethod.POST,
						fullHttpRequest.getUri(), fullHttpRequest.content());
			}
			request.setRemoteAddress(ctx.channel().remoteAddress());
			request.setLocalAddress(ctx.channel().localAddress());
			XXHttpResponse response = new XXHttpResponse();
			if ("/".equals(fullHttpRequest.getUri())) {
				writeResponse(ctx, response, "Welcome Use Netty Server");
				return;
			}
			asyncHttpThreadPool.execute(new HttpRequestTask(ctx, request, response));
		} catch (Exception e) {
			logger.error("请求数据包异常:" + e);
			ctx.close();
		}
	}
	
	/**
     * 异步执行任务
     */
    private class HttpRequestTask implements Runnable{
        private ChannelHandlerContext ctx;
        private XXHttpRequest request;
        private XXHttpResponse response;

        public HttpRequestTask(ChannelHandlerContext ctx, XXHttpRequest request, XXHttpResponse response) {
            this.ctx = ctx;
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            try {
            	dispatch(ctx, request, response);
            } catch (Exception e) {
            	logger.error("处理Cmd失败:",e);
            }
        }
    }

	/**
	 * 路径转发
	 * 
	 * @param request
	 * @param response
	 */
	private void dispatch(ChannelHandlerContext ctx, XXHttpRequest request, XXHttpResponse response) {
		Object result = null;
		try {
			String path = request.getPath();
			RequestMapperHolder requestMapperHolder = abstractAnnotationScanner.getRequestMapperHolder(path);
			BaseHttpService service = requestMapperHolder.getService();
			Method method = requestMapperHolder.getMethod();
			result = method.invoke(service, request, response);
		} catch (Exception e) {
			response.setStatus(HttpResponseStatus.NOT_FOUND);
			result = "404 Not Found";
		} finally {
			writeResponse(ctx, response, result);
		}
	}

	/**
	 * 把结果写入channel
	 * 
	 * @param ctx
	 * @param response
	 * @param result
	 */
	private void writeResponse(ChannelHandlerContext ctx, XXHttpResponse response, Object result) {
		if (!response.getContentSetted()) {
			response.setContent(JsonUtils.objectToJson(result));
			response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE,
					"text/plain; charset=" + response.getResponseCharset());
			response.headers().set(CONTENT_LENGTH, response.getContentLength());
			// TODO 需要设置http长连接的存活时间
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			ctx.writeAndFlush(response);
		}
	}
}
