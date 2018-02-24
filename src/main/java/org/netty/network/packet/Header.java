/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.packet;

/**
 * 消息头
 * @author chenfanglin
 * 2018年2月9日下午12:00:56
 */
public class Header {
    private int len;
    private long headLen;
    private long cmd;
    private int userId;
    private long gameId;
    private long clientSeq;
    private long serverReq;
    private long timeStamp;
    private long bodyLen;
	public int getLen() {
		return len;
	}
	public void setLen(int len) {
		this.len = len;
	}
	public long getHeadLen() {
		return headLen;
	}
	public void setHeadLen(long headLen) {
		this.headLen = headLen;
	}
	public long getCmd() {
		return cmd;
	}
	public void setCmd(long cmd) {
		this.cmd = cmd;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public long getGameId() {
		return gameId;
	}
	public void setGameId(long gameId) {
		this.gameId = gameId;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public long getBodyLen() {
		return bodyLen;
	}
	public void setBodyLen(long bodyLen) {
		this.bodyLen = bodyLen;
	}
	public long getClientSeq() {
		return clientSeq;
	}
	public void setClientSeq(long clientSeq) {
		this.clientSeq = clientSeq;
	}
	public long getServerReq() {
		return serverReq;
	}
	public void setServerReq(long serverReq) {
		this.serverReq = serverReq;
	}
}
