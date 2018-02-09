/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 重写DefaultFullHttpResponse
 * why?
 * 因为netty提供的DefaultFullHttpResponse不能设置content
 * @author chenfanglin
 * 2017年5月12日下午3:08:13
 */
public class XXDefaultFullHttpResponse extends DefaultHttpResponse implements FullHttpResponse{

	private ByteBuf content;
	private final HttpHeaders trailingHeaders;
	private final boolean validateHeaders;

	public XXDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status) {
		this(version, status, Unpooled.buffer(0));
	}

	public XXDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
		this(version, status, content, true);
	}

	public XXDefaultFullHttpResponse(HttpVersion version, HttpResponseStatus status, ByteBuf content,
			boolean validateHeaders) {
		super(version, status, validateHeaders);
		if (content == null) {
			throw new NullPointerException("content");
		}
		this.content = content;
		trailingHeaders = new DefaultHttpHeaders(validateHeaders);
		this.validateHeaders = validateHeaders;
	}

	@Override
	public HttpHeaders trailingHeaders() {
		return trailingHeaders;
	}

	@Override
	public ByteBuf content() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(ByteBuf content) {
		if (content == null) {
			content = Unpooled.EMPTY_BUFFER;
		}
		this.content = content;
	}

	@Override
	public int refCnt() {
		return content.refCnt();
	}

	@Override
	public FullHttpResponse retain() {
		content.retain();
		return this;
	}

	@Override
	public FullHttpResponse retain(int increment) {
		content.retain(increment);
		return this;
	}

	@Override
	public boolean release() {
		return content.release();
	}

	@Override
	public boolean release(int decrement) {
		return content.release(decrement);
	}

	@Override
	public FullHttpResponse setProtocolVersion(HttpVersion version) {
		super.setProtocolVersion(version);
		return this;
	}

	@Override
	public FullHttpResponse setStatus(HttpResponseStatus status) {
		super.setStatus(status);
		return this;
	}

	@Override
	public FullHttpResponse copy() {
		DefaultFullHttpResponse copy = new DefaultFullHttpResponse(getProtocolVersion(), getStatus(), content().copy(),
				validateHeaders);
		copy.headers().set(headers());
		copy.trailingHeaders().set(trailingHeaders());
		return copy;
	}

	@Override
	public FullHttpResponse duplicate() {
		DefaultFullHttpResponse duplicate = new DefaultFullHttpResponse(getProtocolVersion(), getStatus(), content()
				.duplicate(), validateHeaders);
		duplicate.headers().set(headers());
		duplicate.trailingHeaders().set(trailingHeaders());
		return duplicate;
	}

}
