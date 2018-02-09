/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.codec;

import org.netty.network.packet.Header;

/**
 * 命令字对应的命令对象
 * @author chenfanglin
 * 2018年2月9日下午12:57:34
 */
public class Command<T> {

    //命令对象对应的命令字
    private int cmd;

    //消息头对象
    private Header header;

    //消息体
    private T body;

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public T getBody() {
		return body;
	}

	public void setBody(T body) {
		this.body = body;
	}
	
}
