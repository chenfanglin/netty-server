/**
 * Copyright (c) 2018 chenfanglin
 * All rights reserved
 */
package org.netty.network.handle;

import java.util.List;

import org.netty.network.annotation.AbstractAnnotationScanner;
import org.netty.network.codec.Command;
import org.netty.network.codec.CommandDecoder;
import org.netty.network.packet.Header;
import org.netty.network.util.PacketUtil;
import org.netty.network.util.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.GeneratedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

/**
 * 这里解释一下为什么不使用注解？
 * 
 * 当发生断线重连的时候，CommandCodecHandler相当于多次添加到ChannelPipelines中，这是必须添加@ChannelHandler.Sharable注解，
 * 然而每一个CommandCodecHandler都有针对某个socket的累积对象,CommandCodecHandler是一个不可以共享的对象类型，贴出部分源码：
 *  static void ensureNotSharable(ChannelHandlerAdapter handler) {
        if (handler.isSharable()) {
            throw new IllegalStateException("@Sharable annotation is not allowed");
        }
    }
 * 通过源码发现   ByteToMessageCodec 不允许使用@ChannelHandler.Sharable，所以这里需要每次都新建一个CommandCodecHandler
 */
@SuppressWarnings({"rawtypes"})
public class CommandCodecHandler extends ByteToMessageCodec<Command<?>> {

	private static final Logger log = LoggerFactory.getLogger(CommandCodecHandler.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Command<?> command, ByteBuf frame) throws Exception {
		try {
			int headLen = constructPackageHeader(frame, command.getHeader());
			GeneratedMessage generatedMessage = (GeneratedMessage) command.getBody();
			byte[] body = generatedMessage.toByteArray();
			frame.writeBytes(body);
			int bodyLen = headLen + body.length - 4;
			frame.setInt(0, bodyLen);
			if (log.isDebugEnabled()) {
				log.debug("响应客户端成功,包头长度:{},包体长度:{},命令字:{}", headLen, bodyLen,
						Long.toHexString(command.getHeader().getCmd()));
			}
		} catch (Exception e) {
			log.error("响应客户端失败:", e);
		}
	}

	/**
	 * 将接受到的字节转换成对应的Command的实现
	 *
	 * @param ctx
	 * @param frame
	 * @param out
	 * @throws Exception
	 */
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf frame, List<Object> out) throws Exception {
		if (frame == null || frame.readableBytes() == 0) {
			return;
		}
		Header header = PacketUtil.parsePacketHeader(frame);
		if (header != null) {
			if (log.isDebugEnabled()) {
				log.debug("请求头:{}", header);
			}
			byte[] bodyData = PacketUtil.readPacketBody(frame, header);
			// 基于请求头中的命令字返回该具体命令字的编解码
			int cmd = (int) header.getCmd();
			// 获取命令字解码器
			CommandDecoder decoder = SpringUtils.getBean(AbstractAnnotationScanner.class).getDecoder(cmd);
			if (decoder == null) {
				log.warn("没有找到对应的解码器,cmd:{}", cmd);
			} else {
				// 解码命令
				Command command = decoder.decode(header, bodyData);
				out.add(command);
			}
		}
	}

	/**
	 * 构造数据包的头
	 *
	 * @param out
	 * @param header
	 * @return 返回包头的长度
	 */
	private int constructPackageHeader(ByteBuf out, Header header) {
		// 包体原有默认长度
		int headLen = PacketUtil.DEFAULT_HEAD_LEN;
		// 写入包头数据
		out.writeInt(header.getLen());
		out.writeInt((int) header.getHeadLen());
		out.writeInt((int) header.getCmd());
		out.writeInt(header.getUserId());
		out.writeInt((int) header.getGameId());
		out.writeInt((int) header.getClientSeq());
        out.writeInt((int) header.getServerReq());
		out.writeLong(header.getTimeStamp());
		out.setInt(4, headLen);
		return headLen;
	}

}
