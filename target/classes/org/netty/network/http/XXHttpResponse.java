/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.http;

import java.nio.charset.Charset;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class XXHttpResponse extends XXDefaultFullHttpResponse{

	private Charset charsetUTF8 = Charset.forName("UTF-8");
	
	private int contentLength;
	
	private volatile boolean contentSetted = false;
	
	public XXHttpResponse() {
		// 初始化一些默认值
		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
	}

	public void setContent(String result) {
		super.setContent(Unpooled.wrappedBuffer(result.getBytes(charsetUTF8)));
		contentLength = content().readableBytes();
		contentSetted = true;
	}
	
	/**
	 * 302重定向
	 * 
	 * @param localtionUrl
	 *            重定向的URL
	 */
	public void redirect(String localtionUrl) {
		setStatus(HttpResponseStatus.FOUND);
		setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
	}
	
	/**
	 * 重定向
	 * 
	 * @param localtionUrl
	 *            重定向的URL
	 */
	public void redirect(String localtionUrl, HttpResponseStatus status) {
		setStatus(status);
		setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
	}
	
	public Charset getResponseCharset(){
		return charsetUTF8;
	}
	
	public long getContentLength() {
		return contentLength;
	}
	
	public boolean getContentSetted() {
		return contentSetted;
	}
	
	public boolean setHeaderIfEmpty(String name, String value) {
		if (headers().get(name) == null) {
			headers().set(name, value);
			return true;
		}
		return false;
	}
}
