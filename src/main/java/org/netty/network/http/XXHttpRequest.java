/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.http;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

public class XXHttpRequest extends DefaultFullHttpRequest {

	private static Logger logger = LoggerFactory.getLogger(XXHttpRequest.class);

	private static final HttpDataFactory httpDataFactory = new DefaultHttpDataFactory(false);

	private Charset charsetUTF8 = Charset.forName("UTF-8");

	private QueryStringDecoder queryStringDecoder;

	private HttpPostRequestDecoder httpPostRequestDecoder;

	private Map<String, List<String>> parametersByGet;

	private Map<String, List<String>> parametersByPost;

	/**
	 * 远程主机地址
	 */
	private SocketAddress remoteAddress;

	/**
	 * 远程主机IP
	 */
	private String remoteIP;

	/**
	 * 本机地址
	 */
	private SocketAddress localAddress;
	/**
	 * 本机IP
	 */
	private String localIP;
	
	private String url;

	public XXHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
		super(httpVersion, method, uri);
		initParameters();
	}

	public XXHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, ByteBuf content) {
		super(httpVersion, method, uri, content);
		initParameters();
	}

	private void initParameters(){
		getParametersByGet();
		getParametersByPost();
	}
	
	/**
	 * 获取请求参数值,支持获取get和post请求的参数
	 * 
	 * @param key
	 * @return
	 */
	public String getParameter(String key) {
		if (StringUtils.isEmpty(key)) {
			throw new IllegalArgumentException("key isEmpty" + key);
		}
		List<String> v = getParametersByGet().get(key);
		if (v != null) {
			return v.get(0);
		}
		return getParameterByPost(key);
	}

	/**
	 * 获取请求参数值,支持获取get和post请求的参数,如果获取的值是null,那么返回defaultValue
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getParameter(String key, String defaultValue) {
		String value = getParameter(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 注意这里只是把uri中的请求参数提取出来,post中的请求参数需要使用getParametersByPost 获取get请求参数列表
	 * 
	 * @return
	 */
	public Map<String, List<String>> getParametersByGet() {
		if (parametersByGet == null) {
			try {
				Map<String, List<String>> params = getQueryStringDecoder().parameters();
				parametersByGet = params;
			} catch (Exception e) {
				parametersByGet = Collections.emptyMap();
				logger.error("queryString decode fail:" + e);
			}
		}
		return parametersByGet;
	}

	public QueryStringDecoder getQueryStringDecoder() {
		if (queryStringDecoder == null) {
			queryStringDecoder = new QueryStringDecoder(getUri(), charsetUTF8);
		}
		return queryStringDecoder;
	}

	public String getParameterByPost(String key) {
		List<String> v = getParametersByPost().get(key);
		if (v != null) {
			return v.get(0);
		}
		return null;
	}

	public Map<String, List<String>> getParametersByPost() {
		if (parametersByPost == null) {
			HttpPostRequestDecoder httpPostRequestDecoder = getHttpPostRequestDecoder();
			if (httpPostRequestDecoder != null) {
				Map<String, List<String>> paramsMap = new HashMap<String, List<String>>();
				List<InterfaceHttpData> datas = httpPostRequestDecoder.getBodyHttpDatas();
				if (datas != null) {
					for (InterfaceHttpData data : datas) {
						if (data instanceof Attribute) {
							Attribute attribute = (Attribute) data;
							try {
								String key = attribute.getName();
								String value = attribute.getValue();
								List<String> params = paramsMap.get(key);
								if (params == null) {
									params = new ArrayList<String>();
								}
								params.add(value);
								paramsMap.put(key, params);
							} catch (Exception e) {
								logger.error("cant init attribute,req:{},attribute:{}", this, attribute);
							}
						}
					}
				}
				parametersByPost = paramsMap;
			} else {
				parametersByPost = Collections.emptyMap();
			}
		}
		return parametersByPost;
	}

	public HttpPostRequestDecoder getHttpPostRequestDecoder() {
		if (httpPostRequestDecoder == null) {
			HttpMethod method = getMethod();
			if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
				try {
					httpPostRequestDecoder = new HttpPostRequestDecoder(httpDataFactory, this, charsetUTF8);
					httpPostRequestDecoder.offer(this);
				} catch (ErrorDataDecoderException e) {
					logger.error("request postDataDecode error + " + e);
				} catch (Exception e) {
					logger.error("post 解码异常 :" + e);
				}
			}
		}
		return httpPostRequestDecoder;
	}

	/**
	 * 获取http头部报文
	 * 
	 * @param name
	 * @return
	 */
	public String getHeader(String name) {
		String value = headers().get(name);
		return value;
	}

	/**
	 * 获取http头部报文
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getHeader(String name, String defaultValue) {
		String value = headers().get(name);
		if (null == value) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 获取远程主机socketAddress
	 * 
	 * @return
	 */
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @param socketAddress
	 */
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/**
	 * 获取远程主机HostName
	 * 
	 * @return
	 */
	public String getRemoteHost() {
		return ((InetSocketAddress) remoteAddress).getHostName();
	}

	/**
	 * 获取远程主机IP
	 * 
	 * @return
	 */
	public String getRemoteIP() {
		if (remoteIP == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
			if (inetSocketAddress != null) {
				InetAddress addr = inetSocketAddress.getAddress();
				if (addr != null) {
					remoteIP = addr.getHostAddress();
					return remoteIP;
				}
			}
		}
		return remoteIP;
	}

	/**
	 * 获取远程主机端口
	 * 
	 * @return
	 */
	public int getRemotePort() {
		return ((InetSocketAddress) remoteAddress).getPort();
	}

	public void setLocalAddress(SocketAddress localAddress) {
		this.localAddress = localAddress;
	}
	
	/**
	 * 获取本机地址
	 * @return
	 */
	public SocketAddress getLocalAddress() {
		return localAddress;
	}

	/**
	 * 获取本机IP
	 * @return
	 */
	public String getLocalIP() {
		if (localIP == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) getLocalAddress();
			if (inetSocketAddress != null) {
				InetAddress addr = inetSocketAddress.getAddress();
				if (addr != null) {
					localIP = addr.getHostAddress();
					return localIP;
				}
			}
		}
		return localIP;
	}

	/**
	 * 获取本机端口
	 * @return
	 */
	public int getLocalPort() {
		return ((InetSocketAddress) localAddress).getPort();
	}

	/**
	 * 获取完整请求路径
	 * @return
	 */
	public String getUrl() {
		if (url == null) {
			String host = headers().get(HttpHeaders.Names.HOST);
			String port = getLocalPort() == 80 ? "" : ":" + getLocalPort();
			url = "http://" + (StringUtils.isEmpty(host) ? getLocalIP() + port : host) + getUri();
		}
		return url;
	}
	
	/**
	 * 获取请求路径
	 * @return
	 */
	public String getPath() {
		return getQueryStringDecoder().path();
	}
	
	@Override
	public String toString() {
		return this.getRemoteAddress() + "/" + this.getMethod() + " " + this.getUri();
	}
}
